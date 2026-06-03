package pricing.domain.pricing;

import java.math.BigDecimal;

public record DiscountLine(String type, BigDecimal amount) {
}
