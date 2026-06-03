package pricing.domain.promotion;

import pricing.domain.pricing.DiscountLine;
import pricing.domain.pricing.PricingContext;
import pricing.domain.pricing.PricingResult;
import pricing.domain.pricing.PricingResultBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mutable chain state: holds remaining handlers and accumulates discounts via the builder.
 */
public final class PromotionChainContinuation {

    private final List<PromotionChainHandler> handlers;
    private final PricingResultBuilder builder;
    private int index;

    public PromotionChainContinuation(BigDecimal subtotal, List<PromotionChainHandler> handlers) {
        this.handlers = List.copyOf(handlers);
        this.builder = new PricingResultBuilder().subtotal(subtotal);
        this.index = 0;
    }

    public void proceed(PricingContext context) {
        if (index >= handlers.size()) {
            return;
        }
        PromotionChainHandler handler = handlers.get(index++);
        handler.handle(context, this);
    }

    void addDiscount(DiscountLine line) {
        builder.addDiscount(line);
    }

    public PricingResult build() {
        return builder.build();
    }
}
