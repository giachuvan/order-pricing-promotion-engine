package pricing.api;

import pricing.api.dto.CalculateOrderRequest;
import pricing.api.dto.OrderCalculationResponse;
import pricing.application.OrderPricingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderPricingService orderPricingService;

    public OrderController(OrderPricingService orderPricingService) {
        this.orderPricingService = orderPricingService;
    }

    @PostMapping("/calculate")
    public ApiResponse<OrderCalculationResponse> calculate(@Valid @RequestBody CalculateOrderRequest request) {
        return ApiResponse.success(orderPricingService.calculate(request));
    }
}
