package com.hhplusecommerce.api.product.controller;

import com.hhplusecommerce.api.product.dto.PopularProductResponse;
import com.hhplusecommerce.common.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hhplusecommerce.api.product.docs.PopularProductSwaggerDocs.*;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "인기 상품 API", description = "인기 상품 조회 API")
public class PopularProductController {

    @GetMapping("/popular")
    @Operation(summary = "인기 상품 조회", description = "판매량 기준 상위 인기 상품을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = POPULAR_PRODUCT_SUCCESS))),
    })
    public ResponseEntity<ApiResult<List<PopularProductResponse>>> getPopularProducts() {
        List<PopularProductResponse> mockList = List.of(
                new PopularProductResponse(1L, "MacBook Pro", 2390000L, 200),
                new PopularProductResponse(2L, "AirPods Pro", 359000L, 180),
                new PopularProductResponse(3L, "iPhone 15", 1350000L, 150),
                new PopularProductResponse(4L, "iPad Pro", 1190000L, 120),
                new PopularProductResponse(5L, "Apple Watch", 599000L, 100)
        );
        return ResponseEntity.ok(ApiResult.success(mockList));
    }
}
