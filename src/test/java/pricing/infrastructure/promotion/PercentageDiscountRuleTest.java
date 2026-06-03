package pricing.infrastructure.promotion;

import pricing.domain.model.CustomerType;
import pricing.domain.pricing.LineItem;
import pricing.domain.pricing.PricingContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PercentageDiscountRuleTest {

    @Test
    void appliesTenPercentOffSubtotal() {
        PricingContext context = new PricingContext(
                new BigDecimal("250"),
                List.of(new LineItem("A100", new BigDecimal("100"), 2)),
                CustomerType.VIP,
                null
        );

        Optional<pricing.domain.pricing.DiscountLine> result =
                new PercentageDiscountRule(new BigDecimal("10")).apply(context);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("25.00"), result.get().amount());
        assertEquals("PERCENTAGE_DISCOUNT", result.get().type());
    }
}
