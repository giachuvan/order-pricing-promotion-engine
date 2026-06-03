package pricing.application.port;

import pricing.domain.model.PromotionDefinition;

import java.util.List;

public interface ActivePromotionPort {

    List<PromotionDefinition> findActivePromotions();
}
