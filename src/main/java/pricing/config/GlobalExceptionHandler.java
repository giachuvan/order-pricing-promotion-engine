package pricing.config;

import pricing.api.ApiError;
import pricing.api.ApiResponse;
import pricing.api.ErrorCode;
import pricing.application.exception.PricingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PricingException.class)
    public ResponseEntity<ApiResponse<Void>> handlePricing(PricingException ex) {
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(ApiResponse.error(new ApiError(code.name(), ex.getMessage())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        if (isActivePromotionTypeConflict(ex)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(new ApiError(
                            ErrorCode.PROMOTION_CONFLICT.name(),
                            "Active promotion of this type already exists")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(new ApiError(
                        ErrorCode.INTERNAL_ERROR.name(),
                        "A database constraint was violated")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(new ApiError(ErrorCode.VALIDATION_ERROR.name(), message)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(new ApiError(
                        ErrorCode.INTERNAL_ERROR.name(),
                        "An unexpected error occurred")));
    }

    private static boolean isActivePromotionTypeConflict(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains("idx_promotions_type_active")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
