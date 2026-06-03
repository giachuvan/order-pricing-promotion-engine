package pricing.api;

import pricing.application.PromotionUseCase;
import pricing.config.GlobalExceptionHandler;
import pricing.domain.model.Promotion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PromotionController.class)
@Import(GlobalExceptionHandler.class)
class PromotionControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionUseCase promotionUseCase;

    @Test
    void listActiveReturnsPromotions() throws Exception {
        when(promotionUseCase.listActive()).thenReturn(List.of(
                new Promotion(1L, "PERCENTAGE_DISCOUNT", new BigDecimal("10"), true, Instant.parse("2026-01-01T00:00:00Z"))
        ));

        mockMvc.perform(get("/api/v1/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("PERCENTAGE_DISCOUNT"));
    }

    @Test
    void createReturns201() throws Exception {
        when(promotionUseCase.create(any())).thenReturn(
                new Promotion(99L, "VIP_DISCOUNT", new BigDecimal("7"), false, Instant.parse("2026-06-01T12:00:00Z")));

        mockMvc.perform(post("/api/v1/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "VIP_DISCOUNT",
                                  "value": 7,
                                  "active": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(promotionUseCase).create(any());
    }

    @Test
    void createWithBlankTypeReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": " ",
                                  "value": 10,
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(promotionUseCase, never()).create(any());
    }
}
