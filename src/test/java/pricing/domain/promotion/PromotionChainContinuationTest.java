package pricing.domain.promotion;

import pricing.domain.model.CustomerType;
import pricing.domain.pricing.DiscountLine;
import pricing.domain.pricing.LineItem;
import pricing.domain.pricing.PricingContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromotionChainContinuationTest {

    @Test
    void handlersRunInSequenceViaExplicitProceed() {
        List<String> order = new ArrayList<>();
        PricingContext context = new PricingContext(
                new BigDecimal("100"),
                List.of(new LineItem("A", new BigDecimal("100"), 1)),
                CustomerType.REGULAR,
                null
        );

        List<PromotionChainHandler> handlers = List.of(
                (ctx, chain) -> {
                    order.add("first");
                    chain.proceed(ctx);
                },
                (ctx, chain) -> {
                    order.add("second");
                    chain.addDiscount(new DiscountLine("TEST", new BigDecimal("1")));
                    chain.proceed(ctx);
                },
                (ctx, chain) -> order.add("third")
        );

        var continuation = new PromotionChainContinuation(context.subtotal(), handlers);
        continuation.proceed(context);
        var result = continuation.build();

        assertEquals(List.of("first", "second", "third"), order);
        assertEquals(new BigDecimal("99"), result.finalPrice());
    }
}
