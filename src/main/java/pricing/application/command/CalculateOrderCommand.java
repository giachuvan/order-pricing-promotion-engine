package pricing.application.command;

import pricing.domain.model.CustomerType;

import java.util.List;

public record CalculateOrderCommand(
        CustomerType customerType,
        List<OrderLineCommand> items,
        String couponCode
) {
}
