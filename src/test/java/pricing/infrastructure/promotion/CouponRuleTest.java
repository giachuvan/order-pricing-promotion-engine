package pricing.infrastructure.promotion;

import pricing.domain.model.CouponDiscount;
import pricing.domain.model.CustomerType;
import pricing.domain.pricing.PricingContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponRuleTest {

    @Test
    void appliesFlatCouponAmount() {
        CouponDiscount coupon = new CouponDiscount("SUMMER10", new BigDecimal("10"));

        PricingContext context = new PricingContext(
                new BigDecimal("250"),
                List.of(),
                CustomerType.VIP,
                "SUMMER10"
        );

        var result = new CouponRule(coupon).apply(context);

        assertTrue(result.isPresent());
        assertEquals("COUPON_SUMMER10", result.get().type());
        assertEquals(new BigDecimal("10"), result.get().amount());
    }
}
