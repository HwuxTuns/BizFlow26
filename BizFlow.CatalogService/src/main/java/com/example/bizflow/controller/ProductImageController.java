package com.example.bizflow.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    private static final Path ASSETS_ROOT = resolveAssetsRoot();
    private static final Path IMAGE_DIR = ASSETS_ROOT.resolve(Paths.get("img", "img_sanpham"));
    private static final Path IMAGE_INDEX = ASSETS_ROOT.resolve(Paths.get("data", "product-image-files.json"));
    private static final String WEB_PREFIX = "/assets/img/img_sanpham/";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file,
            @RequestParam("productName") String productName) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image file is required"));
        }
        String originalName = file.getOriginalFilename();
        String sanitizedName = sanitizeFilename(originalName);
        String baseName = normalizeFileBase(productName);
        if (baseName.isEmpty()) {
            baseName = "product";
        }
        String ext = getExtension(originalName);
        if (ext.isEmpty()) {
            ext = "jpg";
        }

        try {
            Files.createDirectories(IMAGE_DIR);
            Files.createDirectories(IMAGE_INDEX.getParent());
            String filename = sanitizedName;
            if (filename.isEmpty()) {
                filename = baseName + "." + ext;
            } else if (!filename.contains(".")) {
                filename = filename + "." + ext;
            }
            Path target = IMAGE_DIR.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String webPath = WEB_PREFIX + target.getFileName().toString();
            updateImageIndex(webPath);
            String aliasFilename = baseName + "." + ext;
            if (!aliasFilename.equalsIgnoreCase(target.getFileName().toString())) {
                Path aliasTarget = IMAGE_DIR.resolve(aliasFilename);
                Files.copy(target, aliasTarget, StandardCopyOption.REPLACE_EXISTING);
                String aliasPath = WEB_PREFIX + aliasTarget.getFileName().toString();
                updateImageIndex(aliasPath);
            }
            return ResponseEntity.ok(Map.of("path", webPath));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save image"));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<?> listProductImages() {
        List<String> entries = readIndexEntriesSafe();
        List<String> apiEntries = entries.stream()
                .map(this::toApiPath)
                .filter(path -> path != null && !path.isBlank())
                .collect(Collectors.toList());
        List<String> merged = new ArrayList<>(apiEntries);
        for (String entry : entries) {
            if (entry != null && !entry.isBlank() && !merged.contains(entry)) {
                merged.add(entry);
            }
        }
        return ResponseEntity.ok(merged);
    }

    @GetMapping(path = "/files/{filename:.+}")
    public ResponseEntity<?> serveProductImage(@PathVariable("filename") String filename) {
        if (filename == null || filename.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Filename is required"));
        }
        try {
            Path target = IMAGE_DIR.resolve(filename).normalize();
            if (!target.startsWith(IMAGE_DIR) || !Files.exists(target)) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(target);
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            byte[] content = Files.readAllBytes(target);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to read image"));
        }
    }

    private void updateImageIndex(String webPath) throws IOException {
        List<String> entries = readIndexEntriesSafe();
        boolean exists = entries.stream().anyMatch(path -> path.equalsIgnoreCase(webPath));
        if (!exists) {
            entries.add(webPath);
            writeIndexEntries(entries);
        }
    }

    private List<String> readIndexEntriesSafe() {
        if (!Files.exists(IMAGE_INDEX)) {
            return new ArrayList<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(IMAGE_INDEX.toFile(), new TypeReference<List<String>>() {});
        } catch (IOException ex) {
            // If the index is missing, empty, or corrupted, recover gracefully.
            return new ArrayList<>();
        }
    }

    private void writeIndexEntries(List<String> entries) throws IOException {

        Path parent = IMAGE_INDEX.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ObjectMapper mapper = new ObjectMapper();
        Path tmp = Files.createTempFile(IMAGE_INDEX.getParent(), "product-image-files", ".json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), entries);
        try {
            Files.move(tmp, IMAGE_INDEX, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, IMAGE_INDEX, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    private String normalizeFileBase(String name) {
        if (name == null) {
            return "";
        }
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String cleaned = normalized.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return cleaned;
    }

    private String sanitizeFilename(String name) {
        if (name == null) {
            return "";
        }
        String justName = Paths.get(name).getFileName().toString().trim();
        if (justName.isEmpty()) {
            return "";
        }
        return justName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String toApiPath(String webPath) {
        if (webPath == null || webPath.isBlank()) {
            return "";
        }
        Path name = Paths.get(webPath).getFileName();
        if (name == null) {
            return "";
        }
        return "/api/product-images/files/" + name.toString();
    }

    private static Path resolveAssetsRoot() {
        String override = System.getenv("BIZFLOW_ASSETS_DIR");
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        List<Path> candidates = List.of(
                Paths.get("BizFlow.Frontend", "assets"),
                Paths.get("FE", "assets"),
                Paths.get("frontend", "assets"),
                Paths.get("assets")
        );
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return Paths.get("BizFlow.Frontend", "assets");
    }
}
