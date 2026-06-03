package pricing.api.dto;

import pricing.domain.model.CustomerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CalculateOrderRequest(
        @NotNull CustomerType customerType,
        @NotEmpty @Valid List<OrderItemRequest> items,
        String couponCode
) {
}
