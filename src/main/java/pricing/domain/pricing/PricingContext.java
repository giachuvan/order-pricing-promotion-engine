package pricing.domain.pricing;

import pricing.domain.model.CustomerType;

import java.math.BigDecimal;
import java.util.List;

public record PricingContext(
        BigDecimal subtotal,
        List<LineItem> items,
        CustomerType customerType,
        String couponCode
) {
    public PricingContext {
        items = List.copyOf(items);
    }
}
