package pricing.infrastructure.promotion;

import org.springframework.stereotype.Component;
import pricing.domain.model.CouponDiscount;
import pricing.domain.model.PromotionDefinition;
import pricing.domain.model.PromotionType;
import pricing.domain.promotion.PromotionRule;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@PipelineOrder(40)
public class Buy2Get1FreeRuleSource implements PromotionRuleSource {

    @Override
    public void contribute(
            Map<PromotionType, PromotionDefinition> activeByType,
            Optional<CouponDiscount> coupon,
            List<PromotionRule> rules
    ) {
        if (activeByType.containsKey(PromotionType.BUY2_GET1_FREE)) {
            rules.add(new Buy2Get1FreeRule());
        }
    }
}
