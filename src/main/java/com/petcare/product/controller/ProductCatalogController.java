package com.petcare.product.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.pagination.PageResponse;
import com.petcare.product.dto.ProductCategoryResponse;
import com.petcare.product.dto.ProductCarouselImageResponse;
import com.petcare.product.dto.ProductDetailResponse;
import com.petcare.product.dto.ProductSummaryResponse;
import com.petcare.product.service.ProductCatalogApplicationService;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public product catalog endpoints.
 * No authentication required for browsing products.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class ProductCatalogController {

    private final ProductCatalogApplicationService catalogService;

    public ProductCatalogController(ProductCatalogApplicationService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/product-categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> listCategories() {
        List<ProductCategoryResponse> categories = catalogService.listCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/product-carousel-images")
    public ResponseEntity<ApiResponse<List<ProductCarouselImageResponse>>> listCarouselImages() {
        List<ProductCarouselImageResponse> images = catalogService.listCarouselImages();
        return ResponseEntity.ok(ApiResponse.ok(images));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @Size(max = 50) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ProductSummaryResponse> response = catalogService.listProducts(categoryId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Long id) {
        ProductDetailResponse response = catalogService.getProductDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
