package pricing.domain.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

public final class PricingResultBuilder {

    private BigDecimal subtotal = BigDecimal.ZERO;
    private final List<DiscountLine> discounts = new ArrayList<>();

    public PricingResultBuilder subtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
        return this;
    }

    public PricingResultBuilder addDiscount(DiscountLine line) {
        discounts.add(line);
        return this;
    }

    public PricingResult build() {
        BigDecimal totalDiscount = discounts.stream()
                .map(DiscountLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalPrice = subtotal.subtract(totalDiscount);
        if (finalPrice.compareTo(ZERO) < 0) {
            finalPrice = ZERO;
        }
        return new PricingResult(subtotal, List.copyOf(discounts), totalDiscount, finalPrice);
    }
}
