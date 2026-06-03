package pricing.domain.promotion;

import pricing.domain.pricing.DiscountLine;
import pricing.domain.pricing.PricingContext;
import pricing.domain.pricing.PricingResult;
import pricing.domain.pricing.PricingResultBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromotionChain {

    public PricingResult apply(PricingContext context, List<PromotionRule> rules) {
        PricingResultBuilder builder = new PricingResultBuilder().subtotal(context.subtotal());
        for (PromotionRule rule : rules) {
            rule.apply(context).ifPresent(builder::addDiscount);
        }
        return builder.build();
    }
}
