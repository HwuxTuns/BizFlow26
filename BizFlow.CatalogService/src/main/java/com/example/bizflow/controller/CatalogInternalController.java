package com.example.bizflow.controller;

import com.example.bizflow.entity.Product;
import com.example.bizflow.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/catalog")
public class CatalogInternalController {

    private final ProductRepository productRepository;

    public CatalogInternalController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductSummary>> getProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductSummary> result = products.stream()
                .map(ProductSummary::fromProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductSummary> getProduct(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(ProductSummary.fromProduct(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/products/batch")
    public ResponseEntity<List<ProductCostSummary>> getProductsBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<ProductCostSummary> result = productRepository.findCostViewByIdIn(ids).stream()
                .map(ProductCostSummary::fromView)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<Void> updateProductStock(@PathVariable("id") Long id,
                                                   @RequestBody StockUpdateRequest request) {
        if (id == null || request == null) {
            return ResponseEntity.badRequest().build();
        }
        Integer value = request.stock;
        int newStock = value != null ? value : 0;
        return productRepository.findById(id)
                .map(product -> {
                    product.setStock(newStock);
                    productRepository.save(product);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static class StockUpdateRequest {
        public Integer stock;
    }

    public static class ProductSummary {
        public Long id;
        public Long categoryId;
        public String code;
        public String barcode;
        public String name;
        public Double price;
        public String unit;
        public String status;

        public static ProductSummary fromProduct(Product product) {
            ProductSummary summary = new ProductSummary();
            summary.id = product.getId();
            summary.categoryId = product.getCategoryId();
            summary.code = product.getCode();
            summary.barcode = product.getBarcode();
            summary.name = product.getName();
            summary.price = product.getPrice() != null ? product.getPrice().doubleValue() : null;
            summary.unit = product.getUnit();
            summary.status = product.getStatus();
            return summary;
        }
    }

    public static class ProductCostSummary {
        public Long id;
        public Long categoryId;
        public String code;
        public String name;
        public java.math.BigDecimal costPrice;

        public static ProductCostSummary fromView(ProductRepository.ProductCostView view) {
            ProductCostSummary summary = new ProductCostSummary();
            summary.id = view.getId();
            summary.categoryId = view.getCategoryId();
            summary.code = view.getCode();
            summary.name = view.getName();
            summary.costPrice = view.getCostPrice();
            return summary;
        }
    }
}
