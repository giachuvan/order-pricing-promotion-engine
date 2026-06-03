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
@PipelineOrder(30)
public class CouponRuleSource implements PromotionRuleSource {

    @Override
    public void contribute(
            Map<PromotionType, PromotionDefinition> activeByType,
            Optional<CouponDiscount> coupon,
            List<PromotionRule> rules
    ) {
        coupon.ifPresent(c -> rules.add(new CouponRule(c)));
    }
}
