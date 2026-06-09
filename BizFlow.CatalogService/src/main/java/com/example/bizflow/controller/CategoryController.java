package com.example.bizflow.controller;

import com.example.bizflow.dto.CategoryDTO;
import com.example.bizflow.entity.Category;
import com.example.bizflow.repository.CategoryRepository;
import com.example.bizflow.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryController(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryRepository.findByStatus("active");
        List<CategoryDTO> dtos = categories.stream()
                .map(cat -> {
                    CategoryDTO dto = CategoryDTO.fromEntity(cat);
                    dto.setProductCount(productRepository.findByCategoryId(cat.getId()).size());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesIncludingInactive() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> dtos = categories.stream()
                .map(cat -> {
                    CategoryDTO dto = CategoryDTO.fromEntity(cat);
                    dto.setProductCount(productRepository.findByCategoryId(cat.getId()).size());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getCategoryById(@PathVariable @NonNull Long id) {
        return categoryRepository.findById(id)
                .map(cat -> {
                    CategoryDTO dto = CategoryDTO.fromEntity(cat);
                    dto.setProductCount(productRepository.findByCategoryId(cat.getId()).size());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getProductsByCategory(@PathVariable @NonNull Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productRepository.findByCategoryId(id));
    }

    @GetMapping("/root")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<Category> categories = categoryRepository.findByParentIdIsNull();
        List<CategoryDTO> dtos = categories.stream()
                .filter(cat -> "active".equals(cat.getStatus()))
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getChildCategories(@PathVariable @NonNull Long id) {
        List<Category> categories = categoryRepository.findByParentId(id);
        List<CategoryDTO> dtos = categories.stream()
                .filter(cat -> "active".equals(cat.getStatus()))
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody @NonNull Category category) {
        try {
            if (category.getName() == null || category.getName().isBlank()) {
                return ResponseEntity.badRequest().body("Category name is required");
            }
            Category saved = categoryRepository.save(category);
            return ResponseEntity.ok(CategoryDTO.fromEntity(saved));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error creating category: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable @NonNull Long id, @RequestBody @NonNull Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(category.getName());
                    existing.setDescription(category.getDescription());
                    existing.setParentId(category.getParentId());
                    existing.setStatus(category.getStatus());
                    Category saved = categoryRepository.save(existing);
                    return ResponseEntity.ok(CategoryDTO.fromEntity(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable @NonNull Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    // Soft delete
                    category.setStatus("inactive");
                    categoryRepository.save(category);
                    return ResponseEntity.ok("Category deactivated successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> permanentDeleteCategory(@PathVariable @NonNull Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Kiểm tra có sản phẩm nào không
        if (!productRepository.findByCategoryId(id).isEmpty()) {
            return ResponseEntity.badRequest().body("Cannot delete category with existing products");
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok("Category permanently deleted");
    }
}
