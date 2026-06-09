package com.example.bizflow.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "category_id")
    private Long categoryId;

    // SKU column in the existing DB contains product codes (unique)
    @Column(name = "sku", unique = true, nullable = false)
    private String code;

    @Column(name = "barcode")
    private String barcode;

    // Use `product_name` column which contains the actual product name in existing dump
    @Column(name = "product_name", nullable = false)
    private String name;

    // Legacy columns in the existing DB schema
    @Column(name = "code", nullable = false)
    private String legacyCode;

    @Column(name = "name", nullable = false)
    private String legacyName;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "unit")
    private String unit;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "stock")
    private Integer stock;

    public Product() {
        // Required by JPA and for builds without Lombok annotation processing
    }

    public Product(String code, String name, BigDecimal price) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.status = "active";
    }

    // Explicit getters/setters to ensure availability even without Lombok processing
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return this.barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLegacyCode() {
        return this.legacyCode;
    }

    public void setLegacyCode(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String getLegacyName() {
        return this.legacyName;
    }

    public void setLegacyName(String legacyName) {
        this.legacyName = legacyName;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStock() {
        return this.stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Column(name = "warranty_period")
    private Integer warrantyPeriod = 30;

    public Integer getWarrantyPeriod() {
        return this.warrantyPeriod;
    }

    public void setWarrantyPeriod(Integer warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public BigDecimal getCostPrice() {
        return this.costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    @PrePersist
    void ensureDefaults() {
        if (this.stock == null) {
            this.stock = 20;
        }
        if (this.legacyCode == null) {
            this.legacyCode = this.code;
        }
        if (this.legacyName == null) {
            this.legacyName = this.name;
        }
        if (this.unit == null) {
            this.unit = "sp";
        }
    }
}
