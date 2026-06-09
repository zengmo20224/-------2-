package com.petcare.product.service;

import com.petcare.product.dto.ProductCategoryResponse;
import com.petcare.product.dto.ProductDetailResponse;
import com.petcare.product.dto.ProductSummaryResponse;
import com.petcare.common.pagination.PageResponse;

import java.util.List;

/**
 * Application service for product catalog queries.
 */
public interface ProductCatalogApplicationService {

    /**
     * Lists all active product categories.
     */
    List<ProductCategoryResponse> listCategories();

    /**
     * Lists products with optional categoryId filter, paginated.
     */
    PageResponse<ProductSummaryResponse> listProducts(Long categoryId, int page, int size);

    /**
     * Gets product detail by ID.
     */
    ProductDetailResponse getProductDetail(Long productId);
}
