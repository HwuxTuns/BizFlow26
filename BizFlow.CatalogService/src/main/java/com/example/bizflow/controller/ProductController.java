package com.example.bizflow.controller;

import com.example.bizflow.dto.ProductDTO;
import com.example.bizflow.entity.Product;
import com.example.bizflow.integration.InventoryClient;
import com.example.bizflow.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryClient inventoryClient;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getAllProducts(@RequestParam(value = "search", required = false) String search) {
        try {
            List<Product> products;
            String query = search != null ? search.trim() : "";
            if (!query.isEmpty()) {
                products = productRepository.searchByStatusAndQuery("active", query);
            } else {
                products = productRepository.findByStatus("active");
            }
            boolean showCostPrice = true;
            List<ProductDTO> dtos = products.stream()
                    .map(p -> ProductDTO.fromEntity(p, showCostPrice))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            try {
                List<Product> products;
                String query = search != null ? search.trim() : "";
                if (!query.isEmpty()) {
                    products = productRepository.searchByStatusAndQuery(null, query);
                } else {
                    products = productRepository.findAll();
                }
                boolean showCostPrice = true;
                List<ProductDTO> dtos = products.stream()
                        .map(p -> ProductDTO.fromEntity(p, showCostPrice))
                        .collect(Collectors.toList());
                return ResponseEntity.ok(dtos);
            } catch (Exception fallback) {
                return ResponseEntity.status(500).body("Error fetching products: " + e.getMessage());
            }
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getProductById(@PathVariable @NonNull Long id) {
        try {
            return productRepository.findById(id)
                    .map(product -> {
                        boolean showCostPrice = true;
                        return ResponseEntity.ok(ProductDTO.fromEntity(product, showCostPrice));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching product: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> createProduct(@RequestBody @NonNull Product product) {
        try {
            if (product.getLegacyCode() == null) {
                product.setLegacyCode(product.getCode());
            }
            if (product.getLegacyName() == null) {
                product.setLegacyName(product.getName());
            }
            Product saved = productRepository.save(product);
            Integer savedStock = saved.getStock();
            int initialStock = savedStock != null ? savedStock : 0;
            inventoryClient.receiveStock(saved.getId(), initialStock, saved.getCostPrice(), "Initial stock", null);
            return ResponseEntity.ok(ProductDTO.fromEntity(saved, true));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable @NonNull Long id, @RequestBody @NonNull Product product) {
        try {
            return productRepository.findById(id)
                    .map(existing -> {
                        existing.setName(product.getName());
                        existing.setCode(product.getCode());
                        existing.setLegacyName(product.getName());
                        existing.setLegacyCode(product.getCode());
                        existing.setBarcode(product.getBarcode());
                        existing.setPrice(product.getPrice());
                        existing.setCostPrice(product.getCostPrice());
                        existing.setUnit(product.getUnit());
                        existing.setDescription(product.getDescription());
                        existing.setCategoryId(product.getCategoryId());
                        existing.setStatus(product.getStatus());
                        existing.setWarrantyPeriod(product.getWarrantyPeriod());
                        if (product.getStock() != null) {
                            existing.setStock(product.getStock());
                        }
                        Product saved = productRepository.save(existing);
                        return ResponseEntity.ok(ProductDTO.fromEntity(saved, true));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error updating product: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable @NonNull Long id) {
        try {
            return productRepository.findById(id)
                    .map(product -> {
                        product.setStatus("inactive");
                        productRepository.save(product);
                        return ResponseEntity.ok("Product deactivated successfully");
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error deleting product: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> permanentDeleteProduct(@PathVariable @NonNull Long id) {
        try {
            if (!productRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            productRepository.deleteById(id);
            return ResponseEntity.ok("Product permanently deleted");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error permanently deleting product: " + e.getMessage());
        }
    }

}
