package pricing.api.dto;

import pricing.domain.pricing.PricingResult;

import java.math.BigDecimal;
import java.util.List;

public record OrderCalculationResponse(
        BigDecimal subtotal,
        List<DiscountDto> discounts,
        BigDecimal totalDiscount,
        BigDecimal finalPrice
) {

    public static OrderCalculationResponse from(PricingResult result) {
        return new OrderCalculationResponse(
                result.subtotal(),
                result.discounts().stream().map(DiscountDto::from).toList(),
                result.totalDiscount(),
                result.finalPrice()
        );
    }
}
