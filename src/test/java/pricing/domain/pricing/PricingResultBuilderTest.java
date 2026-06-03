package pricing.domain.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingResultBuilderTest {

    @Test
    void finalPriceIsCappedAtZeroWhenDiscountsExceedSubtotal() {
        var result = new PricingResultBuilder()
                .subtotal(new BigDecimal("50"))
                .addDiscount(new DiscountLine("BIG", new BigDecimal("100")))
                .build();

        assertEquals(new BigDecimal("100"), result.totalDiscount());
        assertEquals(BigDecimal.ZERO, result.finalPrice());
    }
}
