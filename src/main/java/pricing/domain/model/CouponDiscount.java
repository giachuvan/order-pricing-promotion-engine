package pricing.domain.model;

import java.math.BigDecimal;

public record CouponDiscount(String code, BigDecimal amount) {
}
