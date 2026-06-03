package pricing.api.mapper;

import pricing.api.dto.CalculateOrderRequest;
import pricing.application.command.CalculateOrderCommand;
import pricing.application.command.OrderLineCommand;

public final class OrderRequestMapper {

    private OrderRequestMapper() {
    }

    public static CalculateOrderCommand toCommand(CalculateOrderRequest request) {
        return new CalculateOrderCommand(
                request.customerType(),
                request.items().stream()
                        .map(item -> new OrderLineCommand(item.sku(), item.price(), item.quantity()))
                        .toList(),
                request.couponCode()
        );
    }
}
