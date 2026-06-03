package pricing.api;

import pricing.api.dto.CalculateOrderRequest;
import pricing.api.dto.OrderCalculationResponse;
import pricing.api.mapper.OrderRequestMapper;
import pricing.application.OrderPricingUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderPricingUseCase orderPricingUseCase;

    public OrderController(OrderPricingUseCase orderPricingUseCase) {
        this.orderPricingUseCase = orderPricingUseCase;
    }

    @PostMapping("/calculate")
    public ApiResponse<OrderCalculationResponse> calculate(@Valid @RequestBody CalculateOrderRequest request) {
        var result = orderPricingUseCase.calculate(OrderRequestMapper.toCommand(request));
        return ApiResponse.success(OrderCalculationResponse.from(result));
    }
}
