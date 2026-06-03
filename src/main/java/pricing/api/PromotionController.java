package pricing.api;

import pricing.api.dto.CreatePromotionRequest;
import pricing.api.dto.PromotionResponse;
import pricing.api.mapper.PromotionRequestMapper;
import pricing.application.PromotionUseCase;
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

    private final PromotionUseCase promotionUseCase;

    public PromotionController(PromotionUseCase promotionUseCase) {
        this.promotionUseCase = promotionUseCase;
    }

    @GetMapping
    public ApiResponse<List<PromotionResponse>> listActive() {
        return ApiResponse.success(
                promotionUseCase.listActive().stream()
                        .map(PromotionResponse::from)
                        .toList()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PromotionResponse> create(@Valid @RequestBody CreatePromotionRequest request) {
        return ApiResponse.success(
                PromotionResponse.from(promotionUseCase.create(PromotionRequestMapper.toCommand(request)))
        );
    }
}
