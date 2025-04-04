package com.hhplusecommerce.api.product.controller;

import com.hhplusecommerce.api.product.dto.ProductDetailResponse;
import com.hhplusecommerce.api.product.dto.ProductListResponse;
import com.hhplusecommerce.common.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hhplusecommerce.api.product.docs.ProductSwaggerDocs.*;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "상품 API", description = "상품 관련 API")
public class ProductController {

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = PRODUCT_LIST_SUCCESS)))
    })
    public ResponseEntity<ApiResult<List<ProductListResponse>>> getAllProducts() {
        List<ProductListResponse> products = List.of(
                ProductListResponse.of(1L, "MacBook Pro", 2390000L),
                ProductListResponse.of(2L, "AirPods Pro", 359000L)
        );

        return ResponseEntity.ok(ApiResult.success(products));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = PRODUCT_DETAIL_SUCCESS))),
            @ApiResponse(responseCode = "404", description = "조회 실패", content = @Content(
                    examples = {
                            @ExampleObject(name = "존재하지 않는 상품", value = PRODUCT_NOT_FOUND),
                            @ExampleObject(name = "잘못된 ID 형식", value = PRODUCT_INVALID_ID)}))
    })
    public ResponseEntity<ApiResult<ProductDetailResponse>> getProductDetail(@PathVariable("productId") Long productId) {
        ProductDetailResponse detail = ProductDetailResponse.of(
                productId, "MacBook Pro", "노트북", 2390000L, 10
        );

        return ResponseEntity.ok(ApiResult.success(detail));
    }
}
