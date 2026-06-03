package pricing.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import pricing.api.ErrorCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsActivePromotionUniqueIndexViolationToPromotionConflict() {
        var ex = new DataIntegrityViolationException(
                "duplicate key",
                new RuntimeException(
                        "ERROR: duplicate key value violates unique constraint \"idx_promotions_type_active\""));

        var response = handler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("PROMOTION_CONFLICT", response.getBody().error().code());
    }

    @Test
    void mapsCouponNotFoundTo404() {
        var response = handler.handlePricing(
                new pricing.application.exception.PricingException(
                        ErrorCode.COUPON_NOT_FOUND,
                        "Coupon code X was not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("COUPON_NOT_FOUND", response.getBody().error().code());
    }
}
