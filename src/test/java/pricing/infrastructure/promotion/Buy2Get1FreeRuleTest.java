package pricing.infrastructure.promotion;

import pricing.domain.model.CustomerType;
import pricing.domain.pricing.LineItem;
import pricing.domain.pricing.PricingContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Buy2Get1FreeRuleTest {

    @Test
    void oneFreeUnitPerTwoQuantity() {
        PricingContext context = new PricingContext(
                new BigDecimal("250"),
                List.of(
                        new LineItem("A100", new BigDecimal("100"), 2),
                        new LineItem("B200", new BigDecimal("50"), 1)
                ),
                CustomerType.VIP,
                null
        );

        var result = new Buy2Get1FreeRule().apply(context);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("100.00"), result.get().amount());
    }

    @Test
    void omitsWhenNoEligibleQuantity() {
        PricingContext context = new PricingContext(
                new BigDecimal("50"),
                List.of(new LineItem("B200", new BigDecimal("50"), 1)),
                CustomerType.REGULAR,
                null
        );

        assertTrue(new Buy2Get1FreeRule().apply(context).isEmpty());
    }
}
