package pricing.domain.promotion;

import pricing.domain.pricing.PricingContext;

/**
 * Chain of Responsibility link: applies one step, then delegates via {@link PromotionChainContinuation#proceed}.
 */
public interface PromotionChainHandler {

    void handle(PricingContext context, PromotionChainContinuation continuation);
}
