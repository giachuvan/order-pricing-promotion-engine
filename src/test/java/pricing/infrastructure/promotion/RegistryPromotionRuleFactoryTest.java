package pricing.infrastructure.promotion;

import pricing.domain.model.CouponDiscount;
import pricing.domain.model.PromotionDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RegistryPromotionRuleFactoryTest {

    @Test
    void buildsRulesInPipelineOrder() {
        var factory = new RegistryPromotionRuleFactory(List.of(
                new Buy2Get1FreeRuleSource(),
                new PercentageDiscountRuleSource(),
                new CouponRuleSource(),
                new VipDiscountRuleSource()
        ));

        var rules = factory.createRules(
                List.of(
                        new PromotionDefinition("PERCENTAGE_DISCOUNT", new BigDecimal("10")),
                        new PromotionDefinition("VIP_DISCOUNT", new BigDecimal("5")),
                        new PromotionDefinition("BUY2_GET1_FREE", new BigDecimal("1"))
                ),
                Optional.of(new CouponDiscount("SUMMER10", new BigDecimal("10")))
        );

        assertEquals(4, rules.size());
        assertInstanceOf(PercentageDiscountRule.class, rules.get(0));
        assertInstanceOf(VipDiscountRule.class, rules.get(1));
        assertInstanceOf(CouponRule.class, rules.get(2));
        assertInstanceOf(Buy2Get1FreeRule.class, rules.get(3));
    }
}
