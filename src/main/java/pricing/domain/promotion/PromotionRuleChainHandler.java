package pricing.domain.promotion;

import pricing.domain.pricing.PricingContext;

/**
 * Adapts a {@link PromotionRule} strategy into a CoR {@link PromotionChainHandler}.
 */
public final class PromotionRuleChainHandler implements PromotionChainHandler {

    private final PromotionRule rule;

    public PromotionRuleChainHandler(PromotionRule rule) {
        this.rule = rule;
    }

    @Override
    public void handle(PricingContext context, PromotionChainContinuation continuation) {
        rule.apply(context).ifPresent(continuation::addDiscount);
        continuation.proceed(context);
    }
}
