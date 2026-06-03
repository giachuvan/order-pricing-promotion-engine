package pricing.infrastructure.persistence;

import pricing.application.port.ActivePromotionPort;
import pricing.domain.model.PromotionDefinition;
import pricing.infrastructure.persistence.entity.PromotionEntity;
import pricing.infrastructure.persistence.repository.PromotionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivePromotionAdapter implements ActivePromotionPort {

    private final PromotionRepository promotionRepository;

    public ActivePromotionAdapter(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public List<PromotionDefinition> findActivePromotions() {
        return promotionRepository.findByActiveTrue().stream()
                .map(ActivePromotionAdapter::toDomain)
                .toList();
    }

    private static PromotionDefinition toDomain(PromotionEntity entity) {
        return new PromotionDefinition(entity.getType(), entity.getValue());
    }
}
