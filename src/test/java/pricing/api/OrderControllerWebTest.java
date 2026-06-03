package pricing.api;

import pricing.application.OrderPricingUseCase;
import pricing.config.GlobalExceptionHandler;
import pricing.domain.pricing.PricingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderPricingUseCase orderPricingUseCase;

    @Test
    void calculateReturnsMappedResponse() throws Exception {
        when(orderPricingUseCase.calculate(any())).thenReturn(
                new PricingResult(
                        new BigDecimal("250"),
                        List.of(),
                        new BigDecimal("147.50"),
                        new BigDecimal("102.50")
                ));

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerType": "VIP",
                                  "items": [
                                    { "sku": "A100", "price": 100, "quantity": 2 }
                                  ],
                                  "couponCode": "SUMMER10"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalPrice").value(102.50))
                .andExpect(jsonPath("$.error").value(nullValue()));

        verify(orderPricingUseCase).calculate(any());
    }

    @Test
    void emptyItemsReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerType": "VIP",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(orderPricingUseCase, never()).calculate(any());
    }

    @Test
    void zeroQuantityReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerType": "VIP",
                                  "items": [
                                    { "sku": "A100", "price": 100, "quantity": 0 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(orderPricingUseCase, never()).calculate(any());
    }
}
