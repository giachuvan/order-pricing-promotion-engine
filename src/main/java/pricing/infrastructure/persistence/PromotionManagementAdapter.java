package pricing.infrastructure.persistence;

import pricing.application.port.PromotionManagementPort;
import pricing.domain.model.Promotion;
import pricing.infrastructure.persistence.entity.PromotionEntity;
import pricing.infrastructure.persistence.repository.PromotionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class PromotionManagementAdapter implements PromotionManagementPort {

    private final PromotionRepository promotionRepository;

    public PromotionManagementAdapter(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public List<Promotion> findActivePromotions() {
        return promotionRepository.findByActiveTrue().stream()
                .map(PromotionManagementAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByType(String type) {
        return promotionRepository.existsByTypeAndActiveTrue(type);
    }

    @Override
    public Promotion create(String type, BigDecimal value, boolean active) {
        PromotionEntity entity = new PromotionEntity();
        entity.setType(type);
        entity.setValue(value);
        entity.setActive(active);
        entity.setCreatedAt(Instant.now());
        return toDomain(promotionRepository.save(entity));
    }

    private static Promotion toDomain(PromotionEntity entity) {
        return new Promotion(
                entity.getId(),
                entity.getType(),
                entity.getValue(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
