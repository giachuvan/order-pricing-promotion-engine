package pricing.api.dto;

import pricing.domain.model.Product;

import java.math.BigDecimal;

public record ProductResponse(
        String sku,
        String name,
        BigDecimal price
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.sku(), product.name(), product.price());
    }
}
