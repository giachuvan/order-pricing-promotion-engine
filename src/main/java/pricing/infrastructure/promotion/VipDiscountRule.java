package pricing.infrastructure.promotion;

import pricing.domain.model.CustomerType;
import pricing.domain.model.PromotionType;
import pricing.domain.pricing.DiscountLine;
import pricing.domain.pricing.PricingContext;
import pricing.domain.promotion.PromotionRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class VipDiscountRule implements PromotionRule {

    private final BigDecimal percentage;

    public VipDiscountRule(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public Optional<DiscountLine> apply(PricingContext context) {
        if (context.customerType() != CustomerType.VIP) {
            return Optional.empty();
        }
        BigDecimal amount = context.subtotal()
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return Optional.of(new DiscountLine(PromotionType.VIP_DISCOUNT.name(), amount));
    }
}
