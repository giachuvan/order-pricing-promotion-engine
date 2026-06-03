package pricing.api.dto;

import pricing.domain.model.Promotion;

import java.math.BigDecimal;
import java.time.Instant;

public record PromotionResponse(
        Long id,
        String type,
        BigDecimal value,
        boolean active,
        Instant createdAt
) {

    public static PromotionResponse from(Promotion promotion) {
        return new PromotionResponse(
                promotion.id(),
                promotion.type(),
                promotion.value(),
                promotion.active(),
                promotion.createdAt()
        );
    }
}
