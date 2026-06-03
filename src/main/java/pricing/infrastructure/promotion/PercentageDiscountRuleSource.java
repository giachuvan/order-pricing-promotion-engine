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
@PipelineOrder(10)
public class PercentageDiscountRuleSource implements PromotionRuleSource {

    @Override
    public void contribute(
            Map<PromotionType, PromotionDefinition> activeByType,
            Optional<CouponDiscount> coupon,
            List<PromotionRule> rules
    ) {
        PromotionDefinition definition = activeByType.get(PromotionType.PERCENTAGE_DISCOUNT);
        if (definition != null) {
            rules.add(new PercentageDiscountRule(definition.value()));
        }
    }
}
