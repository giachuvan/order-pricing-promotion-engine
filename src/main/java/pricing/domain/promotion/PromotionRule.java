package pricing.domain.promotion;

import pricing.domain.pricing.DiscountLine;
import pricing.domain.pricing.PricingContext;

import java.util.Optional;

public interface PromotionRule {

    Optional<DiscountLine> apply(PricingContext context);
}
