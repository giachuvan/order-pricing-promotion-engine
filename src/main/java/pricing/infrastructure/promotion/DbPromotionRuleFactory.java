package pricing.infrastructure.promotion;

import pricing.domain.model.CouponDiscount;
import pricing.domain.model.PromotionDefinition;
import pricing.domain.model.PromotionType;
import pricing.domain.promotion.PromotionRule;
import pricing.domain.promotion.PromotionRuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the promotion pipeline in a fixed order: PERCENTAGE → VIP → COUPON → BUY2.
 * Discounts are computed independently against the original subtotal, so this order
 * does not change totals — it only affects the sequence of lines in the response.
 */
@Component
public class DbPromotionRuleFactory implements PromotionRuleFactory {

    private static final Logger log = LoggerFactory.getLogger(DbPromotionRuleFactory.class);

    @Override
    public List<PromotionRule> createRules(List<PromotionDefinition> activePromotions, Optional<CouponDiscount> coupon) {
        Map<PromotionType, PromotionDefinition> byType = activePromotions.stream()
                .flatMap(p -> {
                    try {
                        return Stream.of(Map.entry(PromotionType.valueOf(p.type()), p));
                    } catch (IllegalArgumentException e) {
                        log.warn("Skipping promotion with unknown type: {}", p.type());
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        List<PromotionRule> rules = new ArrayList<>();
        addIfPresent(rules, byType, PromotionType.PERCENTAGE_DISCOUNT,
                p -> new PercentageDiscountRule(p.value()));
        addIfPresent(rules, byType, PromotionType.VIP_DISCOUNT,
                p -> new VipDiscountRule(p.value()));
        coupon.ifPresent(c -> rules.add(new CouponRule(c)));
        addIfPresent(rules, byType, PromotionType.BUY2_GET1_FREE, p -> new Buy2Get1FreeRule());
        return rules;
    }

    private void addIfPresent(
            List<PromotionRule> rules,
            Map<PromotionType, PromotionDefinition> byType,
            PromotionType type,
            Function<PromotionDefinition, PromotionRule> factory
    ) {
        PromotionDefinition definition = byType.get(type);
        if (definition != null) {
            rules.add(factory.apply(definition));
        }
    }
}
