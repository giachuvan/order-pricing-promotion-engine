package pricing.application.port;

import pricing.domain.model.CouponDiscount;

import java.util.Optional;

public interface CouponResolutionPort {

    Optional<CouponDiscount> resolve(String couponCode);
}
