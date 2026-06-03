package pricing.infrastructure.promotion;

import pricing.domain.model.CustomerType;
import pricing.domain.pricing.LineItem;
import pricing.domain.pricing.PricingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Buy2Get1FreeRuleTest {

    @ParameterizedTest(name = "qty={0} -> free discount {1}")
    @CsvSource({
            "1, 0",
            "2, 100",
            "3, 100",
            "4, 200",
            "5, 200"
    })
    void freeUnitsFollowFloorDivisionByTwo(int quantity, String expectedDiscount) {
        PricingContext context = new PricingContext(
                new BigDecimal("100").multiply(BigDecimal.valueOf(quantity)),
                List.of(new LineItem("A100", new BigDecimal("100"), quantity)),
                CustomerType.REGULAR,
                null
        );

        var result = new Buy2Get1FreeRule().apply(context);
        BigDecimal expected = new BigDecimal(expectedDiscount);

        if (expected.compareTo(BigDecimal.ZERO) == 0) {
            assertTrue(result.isEmpty());
        } else {
            assertTrue(result.isPresent());
            assertEquals(0, expected.compareTo(result.get().amount()));
        }
    }

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
