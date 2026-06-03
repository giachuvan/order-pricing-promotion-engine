package pricing.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePromotionRequest(
        @NotBlank String type,
        @NotNull BigDecimal value,
        @NotNull Boolean active
) {
}
