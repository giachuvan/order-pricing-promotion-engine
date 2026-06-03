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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assembles the promotion pipeline from all {@link PromotionRuleSource} beans (Spring auto-discovery).
 * Order: PERCENTAGE (10) → VIP (20) → COUPON (30) → BUY2 (40).
 */
@Component
public class RegistryPromotionRuleFactory implements PromotionRuleFactory {

    private static final Logger log = LoggerFactory.getLogger(RegistryPromotionRuleFactory.class);

    private final List<PromotionRuleSource> sources;

    public RegistryPromotionRuleFactory(List<PromotionRuleSource> sources) {
        this.sources = sources.stream()
                .sorted(Comparator.comparingInt(RegistryPromotionRuleFactory::pipelineOrderOf))
                .toList();
    }

    private static int pipelineOrderOf(PromotionRuleSource source) {
        PipelineOrder order = source.getClass().getAnnotation(PipelineOrder.class);
        return order != null ? order.value() : Integer.MAX_VALUE;
    }

    @Override
    public List<PromotionRule> createRules(List<PromotionDefinition> activePromotions, Optional<CouponDiscount> coupon) {
        Map<PromotionType, PromotionDefinition> activeByType = activePromotions.stream()
                .flatMap(RegistryPromotionRuleFactory::toEntry)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        List<PromotionRule> rules = new ArrayList<>();
        for (PromotionRuleSource source : sources) {
            source.contribute(activeByType, coupon, rules);
        }
        return rules;
    }

    private static Stream<Map.Entry<PromotionType, PromotionDefinition>> toEntry(PromotionDefinition promotion) {
        try {
            return Stream.of(Map.entry(PromotionType.valueOf(promotion.type()), promotion));
        } catch (IllegalArgumentException e) {
            log.warn("Skipping promotion with unknown type: {}", promotion.type());
            return Stream.empty();
        }
    }
}
