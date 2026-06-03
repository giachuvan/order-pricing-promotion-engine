package pricing.infrastructure.persistence.repository;

import pricing.infrastructure.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponEntity, String> {

    Optional<CouponEntity> findByCodeAndActiveTrue(String code);
}
