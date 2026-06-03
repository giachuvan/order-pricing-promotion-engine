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

class VipDiscountRuleTest {

    @Test
    void appliesForVipCustomer() {
        PricingContext context = new PricingContext(
                new BigDecimal("250"),
                List.of(),
                CustomerType.VIP,
                null
        );

        var result = new VipDiscountRule(new BigDecimal("5")).apply(context);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("12.50"), result.get().amount());
    }

    @Test
    void skipsForRegularCustomer() {
        PricingContext context = new PricingContext(
                new BigDecimal("250"),
                List.of(),
                CustomerType.REGULAR,
                null
        );

        assertTrue(new VipDiscountRule(new BigDecimal("5")).apply(context).isEmpty());
    }
}
