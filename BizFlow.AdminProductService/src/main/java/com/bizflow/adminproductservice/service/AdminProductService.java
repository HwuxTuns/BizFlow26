package com.bizflow.adminproductservice.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.bizflow.adminproductservice.dto.ProductOverviewDto;
import com.bizflow.adminproductservice.request.ProductStatusUpdateRequest;

public interface AdminProductService {

    List<ProductOverviewDto> listProducts(String query, Boolean active);

    Page<ProductOverviewDto> listProductsPage(String query, Boolean active, int page, int size);

    ProductOverviewDto getProduct(Long id);

    ProductOverviewDto updateProductStatus(Long id, ProductStatusUpdateRequest request);
}
