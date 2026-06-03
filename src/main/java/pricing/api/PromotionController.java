package pricing.api;

import pricing.api.dto.CreatePromotionRequest;
import pricing.api.dto.PromotionResponse;
import pricing.application.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ApiResponse<List<PromotionResponse>> listActive() {
        return ApiResponse.success(promotionService.listActive());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PromotionResponse> create(@Valid @RequestBody CreatePromotionRequest request) {
        return ApiResponse.success(promotionService.create(request));
    }
}
