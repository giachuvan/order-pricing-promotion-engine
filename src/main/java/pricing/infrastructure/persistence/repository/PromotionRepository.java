package pricing.infrastructure.persistence.repository;

import pricing.infrastructure.persistence.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    List<PromotionEntity> findByActiveTrue();

    boolean existsByTypeAndActiveTrue(String type);
}
