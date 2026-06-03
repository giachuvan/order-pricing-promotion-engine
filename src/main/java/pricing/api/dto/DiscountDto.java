package pricing.api.dto;

import pricing.domain.pricing.DiscountLine;

import java.math.BigDecimal;

public record DiscountDto(String type, BigDecimal amount) {

    public static DiscountDto from(DiscountLine line) {
        return new DiscountDto(line.type(), line.amount());
    }
}
