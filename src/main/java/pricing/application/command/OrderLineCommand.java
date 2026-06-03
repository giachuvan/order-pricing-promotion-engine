package pricing.application.command;

import java.math.BigDecimal;

public record OrderLineCommand(
        String sku,
        BigDecimal price,
        int quantity
) {
}
