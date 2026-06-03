package pricing.application.command;

import java.math.BigDecimal;

public record CreatePromotionCommand(
        String type,
        BigDecimal value,
        Boolean active
) {
}
