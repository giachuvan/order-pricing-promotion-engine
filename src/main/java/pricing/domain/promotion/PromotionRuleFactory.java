package pricing.domain.promotion;

import pricing.domain.model.CouponDiscount;
import pricing.domain.model.PromotionDefinition;

import java.util.List;
import java.util.Optional;

public interface PromotionRuleFactory {

    List<PromotionRule> createRules(List<PromotionDefinition> activePromotions, Optional<CouponDiscount> coupon);
}
