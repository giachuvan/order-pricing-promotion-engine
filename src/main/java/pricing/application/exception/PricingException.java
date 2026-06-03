package pricing.application.exception;

import pricing.api.ErrorCode;

public class PricingException extends RuntimeException {

    private final ErrorCode errorCode;

    public PricingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
