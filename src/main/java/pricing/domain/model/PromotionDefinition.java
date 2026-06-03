package pricing.domain.model;

import java.math.BigDecimal;

public record PromotionDefinition(String type, BigDecimal value) {
}
