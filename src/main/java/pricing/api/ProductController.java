package pricing.api;

import pricing.api.dto.ProductResponse;
import pricing.application.ProductCatalogUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductCatalogUseCase productCatalogUseCase;

    public ProductController(ProductCatalogUseCase productCatalogUseCase) {
        this.productCatalogUseCase = productCatalogUseCase;
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> listProducts() {
        return ApiResponse.success(
                productCatalogUseCase.listProducts().stream()
                        .map(ProductResponse::from)
                        .toList()
        );
    }
}
