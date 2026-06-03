package pricing.domain.promotion;

import pricing.domain.pricing.PricingContext;
import pricing.domain.pricing.PricingResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromotionChain {

    public PricingResult apply(PricingContext context, List<PromotionRule> rules) {
        List<PromotionChainHandler> handlers = rules.stream()
                .<PromotionChainHandler>map(PromotionRuleChainHandler::new)
                .toList();

        PromotionChainContinuation continuation = new PromotionChainContinuation(context.subtotal(), handlers);
        continuation.proceed(context);
        return continuation.build();
    }
}
