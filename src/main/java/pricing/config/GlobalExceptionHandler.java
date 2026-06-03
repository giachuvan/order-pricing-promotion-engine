package pricing.config;

import pricing.api.ApiError;
import pricing.api.ApiResponse;
import pricing.api.ErrorCode;
import pricing.application.exception.PricingException;
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
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(new ApiError(ex.getErrorCode().name(), ex.getMessage())));
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
}
