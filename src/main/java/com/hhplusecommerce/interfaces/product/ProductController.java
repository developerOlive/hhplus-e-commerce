package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.application.popularProduct.PopularProductFacade;
import com.hhplusecommerce.domain.popularProduct.service.PopularProductRankingService;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.product.ProductResult;
import com.hhplusecommerce.domain.product.ProductService;
import com.hhplusecommerce.interfaces.product.ProductRequest.PopularProductSearchRequest;
import com.hhplusecommerce.interfaces.product.ProductRequest.ProductSearchRequest;
import com.hhplusecommerce.interfaces.product.ProductResponse.PopularProductResponse;
import com.hhplusecommerce.interfaces.product.ProductResponse.ProductsResponse;
import com.hhplusecommerce.support.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.hhplusecommerce.interfaces.product.ProductSwaggerDocs.POPULAR_PRODUCT_SUCCESS;
import static com.hhplusecommerce.interfaces.product.ProductSwaggerDocs.PRODUCT_LIST_SUCCESS;

@RestController
@RequiredArgsConstructor
@Tag(name = "상품 API", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;
    private final PopularProductRankingService popularProductRankingService;
    private final PopularProductFacade popularProductFacade;

    @GetMapping("/api/v1/products")
    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = PRODUCT_LIST_SUCCESS)))
    })
    public ResponseEntity<ApiResult<Page<ProductsResponse>>> getProducts(@ModelAttribute @Validated ProductSearchRequest request) {
        Page<ProductResult> productPage = productService.getProductsWithInventory(request.toCommand(), request.toPageable());
        Page<ProductsResponse> responsePage = productPage.map(ProductsResponse::from);

        return ResponseEntity.ok(ApiResult.success(responsePage));
    }

    @GetMapping("/api/v1/products/popular")
    @Operation(
            summary = "인기 상품 조회 (DB + 캐시 기반)",
            description = "캐시 미스 시 DB에서 조회하는 캐시-어사이드 전략으로 인기 상품을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(examples = @ExampleObject(value = POPULAR_PRODUCT_SUCCESS)))
    })
    public ResponseEntity<ApiResult<List<PopularProductResponse>>> getPopularProductsV1(@Valid PopularProductSearchRequest request) {
        List<PopularProduct> products = popularProductFacade.getPopularProducts(request.toCommand());
        List<PopularProductResponse> response = products.stream()
                .map(PopularProductResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
            summary = "인기 상품 조회 (실시간 Redis 랭킹 기반)",
            description = "Redis Sorted Set을 사용해 실시간 판매량 랭킹을 집계하고 조회합니다."
    )
    @GetMapping("/api/v2/products/popular")
    public ResponseEntity<ApiResult<List<PopularProductResponse>>> getPopularProductsV2(@Valid PopularProductSearchRequest request) {
        List<PopularProduct> products = popularProductRankingService.getTopPopularProducts(request.toRankingCommand());
        List<PopularProductResponse> response = products.stream()
                .map(PopularProductResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResult.success(response));
    }
}
