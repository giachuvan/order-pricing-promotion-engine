package pricing.api.mapper;

import pricing.api.dto.CreatePromotionRequest;
import pricing.application.command.CreatePromotionCommand;

public final class PromotionRequestMapper {

    private PromotionRequestMapper() {
    }

    public static CreatePromotionCommand toCommand(CreatePromotionRequest request) {
        return new CreatePromotionCommand(request.type(), request.value(), request.active());
    }
}
