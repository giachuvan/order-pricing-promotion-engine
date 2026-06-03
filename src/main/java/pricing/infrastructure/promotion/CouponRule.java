package pricing.infrastructure.promotion;

import pricing.domain.model.CouponDiscount;
import pricing.domain.pricing.DiscountLine;
import pricing.domain.pricing.PricingContext;
import pricing.domain.promotion.PromotionRule;

import java.util.Optional;

public class CouponRule implements PromotionRule {

    private final CouponDiscount coupon;

    public CouponRule(CouponDiscount coupon) {
        this.coupon = coupon;
    }

    @Override
    public Optional<DiscountLine> apply(PricingContext context) {
        String type = "COUPON_" + coupon.code();
        return Optional.of(new DiscountLine(type, coupon.amount()));
    }
}
