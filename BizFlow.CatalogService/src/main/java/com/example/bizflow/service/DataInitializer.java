package com.example.bizflow.service;

import com.example.bizflow.entity.Product;
import com.example.bizflow.integration.InventoryClient;
import com.example.bizflow.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    public DataInitializer(ProductRepository productRepository, InventoryClient inventoryClient) {
        this.productRepository = productRepository;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        long count = productRepository.count();
        if (count > 0) {
            logger.info("Products already exist ({}). Checking for inactive status to fix.", count);
            List<Product> all = productRepository.findAll();
            List<Product> toEnable = new ArrayList<>();
            for (Product p : all) {
                if (p.getStatus() == null || "inactive".equalsIgnoreCase(p.getStatus())) {
                    p.setStatus("active");
                    toEnable.add(p);
                }
            }
            if (!toEnable.isEmpty()) {
                productRepository.saveAll(toEnable);
                logger.info("Updated {} products to active=true.", toEnable.size());
            } else {
                logger.info("No products needed active=true fixup.");
            }
            seedInventoryStocksIfNeeded();
            return;
        }

        logger.info("No products found. Seeding sample products from classpath:data/products.csv");
        ClassPathResource resource = new ClassPathResource("data/products.csv");
        List<Product> products = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                String[] cols = line.split(",");
                if (cols.length < 3) {
                    continue;
                }
                String code = cols[0].trim();
                String name = cols[1].trim();
                BigDecimal price = new BigDecimal(cols[2].trim());
                Product p = new Product(code, name, price);
                if (cols.length >= 4 && !cols[3].trim().isEmpty()) {
                    try {
                        p.setCategoryId(Long.valueOf(cols[3].trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                products.add(p);
            }
        }

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
            logger.info("Seeded {} products.", products.size());
            seedInventoryStocksIfNeeded();
        } else {
            logger.warn("No products found in resource data/products.csv to seed.");
        }
    }

    private void seedInventoryStocksIfNeeded() {
        List<Product> all = productRepository.findAll();
        if (all.isEmpty()) {
            return;
        }
        List<Long> ids = all.stream()
                .map(Product::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        List<InventoryClient.StockItem> stocks = inventoryClient.getStocks(ids);
        Map<Long, Integer> stockMap = new HashMap<>();
        for (InventoryClient.StockItem item : stocks) {
            if (item != null && item.getProductId() != null) {
                stockMap.put(item.getProductId(), item.getStock() == null ? 0 : item.getStock());
            }
        }
        for (Product product : all) {
            if (product.getId() == null) {
                continue;
            }
            Integer stock = stockMap.get(product.getId());
            if (stock == null || stock == 0) {
                inventoryClient.receiveStock(product.getId(), 20, product.getCostPrice(), "Initial stock", null);
            }
        }
    }
}
