package pricing.domain.promotion;

import pricing.domain.model.CouponDiscount;
import pricing.domain.model.CustomerType;
import pricing.domain.pricing.LineItem;
import pricing.domain.pricing.PricingContext;
import pricing.infrastructure.promotion.Buy2Get1FreeRule;
import pricing.infrastructure.promotion.CouponRule;
import pricing.infrastructure.promotion.PercentageDiscountRule;
import pricing.infrastructure.promotion.VipDiscountRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromotionChainCombinedTest {

    @Test
    void assignmentExampleProducesExpectedFinalPrice() {
        PricingContext context = new PricingContext(
                new BigDecimal("250"),
                List.of(
                        new LineItem("A100", new BigDecimal("100"), 2),
                        new LineItem("B200", new BigDecimal("50"), 1)
                ),
                CustomerType.VIP,
                "SUMMER10"
        );

        CouponDiscount coupon = new CouponDiscount("SUMMER10", new BigDecimal("10"));

        List<PromotionRule> rules = List.of(
                new PercentageDiscountRule(new BigDecimal("10")),
                new VipDiscountRule(new BigDecimal("5")),
                new CouponRule(coupon),
                new Buy2Get1FreeRule()
        );

        var result = new PromotionChain().apply(context, rules);

        assertEquals(new BigDecimal("250"), result.subtotal());
        assertEquals(new BigDecimal("147.50"), result.totalDiscount());
        assertEquals(new BigDecimal("102.50"), result.finalPrice());
        assertEquals(4, result.discounts().size());
    }
}
