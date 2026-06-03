package pricing.application;

import pricing.api.ErrorCode;
import pricing.api.dto.CreatePromotionRequest;
import pricing.api.dto.PromotionResponse;
import pricing.application.exception.PricingException;
import pricing.application.port.PromotionManagementPort;
import pricing.domain.model.PromotionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionService {

    private final PromotionManagementPort promotionManagementPort;

    public PromotionService(PromotionManagementPort promotionManagementPort) {
        this.promotionManagementPort = promotionManagementPort;
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> listActive() {
        return promotionManagementPort.findActivePromotions().stream()
                .map(PromotionResponse::from)
                .toList();
    }

    @Transactional
    public PromotionResponse create(CreatePromotionRequest request) {
        try {
            PromotionType.valueOf(request.type());
        } catch (IllegalArgumentException ex) {
            throw new PricingException(
                    ErrorCode.INVALID_PROMOTION_TYPE,
                    "Unknown promotion type: " + request.type());
        }

        if (Boolean.TRUE.equals(request.active())
                && promotionManagementPort.existsActiveByType(request.type())) {
            throw new PricingException(
                    ErrorCode.PROMOTION_CONFLICT,
                    "Active promotion of type " + request.type() + " already exists");
        }

        return PromotionResponse.from(promotionManagementPort.create(
                request.type(),
                request.value(),
                request.active()
        ));
    }
}
