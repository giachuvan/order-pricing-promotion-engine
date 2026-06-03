package pricing.domain.pricing;

import java.math.BigDecimal;
import java.util.List;

public record PricingResult(
        BigDecimal subtotal,
        List<DiscountLine> discounts,
        BigDecimal totalDiscount,
        BigDecimal finalPrice
) {
}
