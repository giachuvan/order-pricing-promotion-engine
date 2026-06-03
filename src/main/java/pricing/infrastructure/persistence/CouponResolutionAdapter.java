package pricing.infrastructure.persistence;

import pricing.api.ErrorCode;
import pricing.application.exception.PricingException;
import pricing.application.port.CouponResolutionPort;
import pricing.domain.model.CouponDiscount;
import pricing.infrastructure.persistence.entity.CouponEntity;
import pricing.infrastructure.persistence.repository.CouponRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class CouponResolutionAdapter implements CouponResolutionPort {

    private final CouponRepository couponRepository;

    public CouponResolutionAdapter(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public Optional<CouponDiscount> resolve(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }
        String code = couponCode.trim();

        Optional<CouponEntity> active = couponRepository.findByCodeAndActiveTrue(code);
        if (active.isEmpty()) {
            Optional<CouponEntity> existing = couponRepository.findById(code);
            if (existing.isEmpty()) {
                throw new PricingException(
                        ErrorCode.COUPON_NOT_FOUND,
                        "Coupon code " + code + " was not found");
            }
            throw new PricingException(
                    ErrorCode.COUPON_INACTIVE,
                    "Coupon " + code + " is not active");
        }

        CouponEntity entity = active.get();
        if (entity.getExpiryDate().isBefore(LocalDate.now())) {
            throw new PricingException(
                    ErrorCode.COUPON_EXPIRED,
                    "Coupon " + code + " has expired");
        }
        return Optional.of(toDomain(entity));
    }

    private static CouponDiscount toDomain(CouponEntity entity) {
        return new CouponDiscount(entity.getCode(), entity.getDiscountAmount());
    }
}
