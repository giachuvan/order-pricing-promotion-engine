package pricing.domain.pricing;

import java.math.BigDecimal;

public record LineItem(String sku, BigDecimal price, int quantity) {
}
