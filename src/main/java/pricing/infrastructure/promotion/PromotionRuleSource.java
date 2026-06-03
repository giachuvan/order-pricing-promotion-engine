package pricing.infrastructure.promotion;

import pricing.domain.model.CouponDiscount;
import pricing.domain.model.PromotionDefinition;
import pricing.domain.model.PromotionType;
import pricing.domain.promotion.PromotionRule;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contributes zero or one rule to the pipeline. New promotion types register by adding a {@code @Component}
 * implementation without modifying the registry factory.
 */
public interface PromotionRuleSource {

    void contribute(
            Map<PromotionType, PromotionDefinition> activeByType,
            Optional<CouponDiscount> coupon,
            List<PromotionRule> rules
    );
}
