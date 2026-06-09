package com.example.bizflow.service;

import com.example.bizflow.dto.NewProductPurchaseRequest;
import com.example.bizflow.dto.ProductCostHistoryDTO;
import com.example.bizflow.dto.ProductCostUpdateRequest;
import com.example.bizflow.entity.Product;
import com.example.bizflow.entity.ProductCost;
import com.example.bizflow.entity.ProductCostHistory;
import com.example.bizflow.integration.InventoryClient;
import com.example.bizflow.repository.ProductCostHistoryRepository;
import com.example.bizflow.repository.ProductCostRepository;
import com.example.bizflow.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductCostService {

    private final ProductCostRepository productCostRepository;
    private final ProductCostHistoryRepository costHistoryRepository;
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    public ProductCostService(ProductCostRepository productCostRepository,
            ProductCostHistoryRepository costHistoryRepository,
            ProductRepository productRepository,
            InventoryClient inventoryClient) {
        this.productCostRepository = productCostRepository;
        this.costHistoryRepository = costHistoryRepository;
        this.productRepository = productRepository;
        this.inventoryClient = inventoryClient;
    }

    public Optional<BigDecimal> getCurrentCostPrice(Long productId) {
        return productCostRepository.findByProductId(productId)
                .map(ProductCost::getCostPrice);
    }

    public Optional<ProductCost> getProductCost(Long productId) {
        return productCostRepository.findByProductId(productId);
    }

    @Transactional
    public ProductCost updateCostPrice(ProductCostUpdateRequest request, Long userId) {
        Long productId = request.getProductId();
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        BigDecimal newCostPrice = resolveCostPrice(productId, request.getCostPrice());
        if (newCostPrice == null) {
            throw new IllegalArgumentException("Cost price is required");
        }
        Integer qtyValue = request.getQuantity();
        Integer quantity = qtyValue != null ? qtyValue : 0;
        String note = request.getNote();

        ProductCost productCost = productCostRepository.findByProductId(productId)
                .orElse(new ProductCost(productId, newCostPrice));

        productCost.setCostPrice(newCostPrice);
        productCost = productCostRepository.save(productCost);

        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isPresent()) {
            Product product = optProduct.get();
            product.setCostPrice(newCostPrice);
            productRepository.saveAndFlush(product);
        }

        if (quantity > 0) {
            updateInventoryStock(productId, quantity, newCostPrice, note, userId);
        }

        ProductCostHistory history = new ProductCostHistory(
                productId, newCostPrice, quantity, note, userId);
        costHistoryRepository.save(history);

        return productCost;
    }

    @Transactional
    public void updateInventoryStock(Long productId, Integer quantityIn, BigDecimal costPrice, String note, Long userId) {
        inventoryClient.receiveStock(productId, quantityIn, costPrice, note, userId);
    }

    @Transactional
    public ProductCostHistory recordPurchase(Long productId, BigDecimal costPrice,
            Integer quantity, String note, Long userId) {
        ProductCostUpdateRequest request = new ProductCostUpdateRequest();
        request.setProductId(productId);
        request.setCostPrice(costPrice);
        request.setQuantity(quantity);
        request.setNote(note);

        updateCostPrice(request, userId);

        List<ProductCostHistory> histories = costHistoryRepository
                .findByProductIdOrderByCreatedAtDesc(productId);
        return histories.isEmpty() ? null : histories.get(0);
    }

    @Transactional
    public ProductCostHistory createProductAndPurchase(NewProductPurchaseRequest request, Long userId) {
        if (request == null) {
            throw new IllegalArgumentException("Missing request");
        }
        String code = request.getCode() != null ? request.getCode().trim() : "";
        String name = request.getName() != null ? request.getName().trim() : "";
        if (code.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Product code and name are required");
        }
        if (productRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Product code already exists");
        }
        BigDecimal price = request.getPrice();
        BigDecimal costPrice = request.getCostPrice();
        Integer quantity = request.getQuantity();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost price is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setLegacyCode(code);
        product.setLegacyName(name);
        product.setBarcode(normalizeOptional(request.getBarcode()));
        product.setPrice(price);
        product.setCostPrice(costPrice);
        product.setUnit(normalizeOptional(request.getUnit()));
        product.setDescription(normalizeOptional(request.getDescription()));
        product.setCategoryId(request.getCategoryId());
        product.setStatus("active");
        product.setStock(0);
        Product saved = productRepository.save(product);

        ProductCost productCost = new ProductCost(saved.getId(), costPrice);
        productCostRepository.save(productCost);

        ProductCostHistory history = new ProductCostHistory(
                saved.getId(),
                costPrice,
                quantity,
                request.getNote(),
                userId
        );
        costHistoryRepository.save(history);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    inventoryClient.receiveStock(saved.getId(), quantity, costPrice, request.getNote(), userId);
                }
            });
        } else {
            inventoryClient.receiveStock(saved.getId(), quantity, costPrice, request.getNote(), userId);
        }

        return history;
    }

    private BigDecimal resolveCostPrice(Long productId, BigDecimal inputCost) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (inputCost != null) {
            return inputCost;
        }
        Optional<BigDecimal> currentCost = productCostRepository.findByProductId(productId)
                .map(ProductCost::getCostPrice);
        if (currentCost.isPresent()) {
            return currentCost.get();
        }
        Optional<Product> product = productRepository.findById(productId);
        return product.map(Product::getCostPrice).orElse(null);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public List<ProductCostHistoryDTO> getCostHistory(Long productId) {
        if (productId == null) {
            return List.of();
        }
        return costHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(ProductCostHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProductCostHistoryDTO> getAllCostHistory() {
        return costHistoryRepository.findAllWithProduct()
                .stream()
                .map(ProductCostHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProductCostHistoryDTO> getCostHistoryByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return costHistoryRepository.findByDateRange(start, end)
                .stream()
                .map(ProductCostHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProductCostHistoryDTO> getProductCostHistoryByDateRange(
            Long productId, LocalDate startDate, LocalDate endDate) {
        if (productId == null) {
            return List.of();
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return costHistoryRepository.findByProductIdAndDateRange(productId, start, end)
                .stream()
                .map(ProductCostHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void initializeCostsFromProducts() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (product.getCostPrice() != null && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                if (!productCostRepository.existsByProductId(product.getId())) {
                    ProductCost cost = new ProductCost(product.getId(), product.getCostPrice());
                    productCostRepository.save(cost);
                }
            }
        }
    }
}
