package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.applicatoin.product.ProductFacade;
import com.hhplusecommerce.applicatoin.product.result.ProductResult.ProductListResult;
import com.hhplusecommerce.domain.product.ProductService;
import com.hhplusecommerce.support.response.ApiResult;
import com.hhplusecommerce.interfaces.product.ProductResponse.PopularProductResponse;
import com.hhplusecommerce.interfaces.product.ProductResponse.ProductListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hhplusecommerce.interfaces.product.ProductSwaggerDocs.POPULAR_PRODUCT_SUCCESS;
import static com.hhplusecommerce.interfaces.product.ProductSwaggerDocs.PRODUCT_LIST_SUCCESS;

@RestController
@RequiredArgsConstructor
@Tag(name = "상품 API", description = "상품 관련 API")
public class ProductController {

    private final ProductFacade productFacade;
    private final ProductService productService;

    @GetMapping("/api/v1/products")
    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = PRODUCT_LIST_SUCCESS)))
    })
    public ResponseEntity<ApiResult<List<ProductListResponse>>> getProducts(@Valid ProductListRequest request) {
        List<ProductListResult> results = productFacade.getProducts(request.toCriteria());

        List<ProductListResponse> response = results.stream()
                .map(ProductListResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/api/v1/products/popular")
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
