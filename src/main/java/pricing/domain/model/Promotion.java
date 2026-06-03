package pricing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Promotion(
        Long id,
        String type,
        BigDecimal value,
        boolean active,
        Instant createdAt
) {
}
