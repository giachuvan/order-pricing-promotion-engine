package pricing.domain.model;

import java.math.BigDecimal;

public record OrderLine(String sku, BigDecimal price, int quantity, BigDecimal lineTotal) {
}
