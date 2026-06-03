package pricing.infrastructure.promotion;

import pricing.domain.model.PromotionType;
import pricing.domain.pricing.DiscountLine;
import pricing.domain.pricing.LineItem;
import pricing.domain.pricing.PricingContext;
import pricing.domain.promotion.PromotionRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class Buy2Get1FreeRule implements PromotionRule {

    @Override
    public Optional<DiscountLine> apply(PricingContext context) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (LineItem item : context.items()) {
            int freeUnits = item.quantity() / 2;
            if (freeUnits > 0) {
                totalDiscount = totalDiscount.add(item.price().multiply(BigDecimal.valueOf(freeUnits)));
            }
        }
        if (totalDiscount.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }
        return Optional.of(new DiscountLine(
                PromotionType.BUY2_GET1_FREE.name(),
                totalDiscount.setScale(2, RoundingMode.HALF_UP)));
    }
}
