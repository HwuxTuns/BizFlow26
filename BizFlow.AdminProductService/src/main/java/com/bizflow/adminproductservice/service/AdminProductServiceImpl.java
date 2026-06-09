package com.bizflow.adminproductservice.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.adminproductservice.dto.ProductOverviewDto;
import com.bizflow.adminproductservice.entity.Product;
import com.bizflow.adminproductservice.exception.ProductNotFoundException;
import com.bizflow.adminproductservice.repository.ProductRepository;
import com.bizflow.adminproductservice.request.ProductStatusUpdateRequest;

@Service
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;

    public AdminProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductOverviewDto> listProducts(String query, Boolean active) {
        return productRepository
            .searchProducts(query, active, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "updatedAt", "id")))
            .getContent()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

        @Override
        @Transactional(readOnly = true)
        public Page<ProductOverviewDto> listProductsPage(String query, Boolean active, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 200);

        return productRepository
            .searchProducts(query, active, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt", "id")))
            .map(this::toDto);
        }

    @Override
    @Transactional(readOnly = true)
    public ProductOverviewDto getProduct(Long id) {
        return productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional
    public ProductOverviewDto updateProductStatus(Long id, ProductStatusUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setActive(request.getActive());
        return toDto(productRepository.save(product));
    }

    private ProductOverviewDto toDto(Product product) {
        // Lấy category name từ categoryId nếu cần, tạm thời để null hoặc "N/A"
        String categoryName = product.getCategoryId() != null ? "Category " + product.getCategoryId() : "N/A";
        
        return new ProductOverviewDto(
                product.getId(),
                product.getSku(),
                product.getProductName(),
                categoryName,
                product.getDescription(),  // Thêm description từ database
                product.getActive() != null ? product.getActive() : Boolean.TRUE,
                Objects.requireNonNullElse(product.getStock(), 0),
                product.getPrice() != null ? product.getPrice().doubleValue() : 0.0,
                null  // Tạm thời bỏ updatedAt
        );
    }

}
