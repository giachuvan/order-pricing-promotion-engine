package pricing.api;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR,
    INTERNAL_ERROR,
    COUPON_NOT_FOUND,
    COUPON_INACTIVE,
    COUPON_EXPIRED,
    INVALID_PROMOTION_TYPE,
    PROMOTION_CONFLICT;

    public HttpStatus httpStatus() {
        return switch (this) {
            case COUPON_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
