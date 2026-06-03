package pricing.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Order(
        UUID id,
        CustomerType customerType,
        String couponCode,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal finalPrice,
        List<OrderLine> items
) {
    public Order {
        items = List.copyOf(items);
    }
}
