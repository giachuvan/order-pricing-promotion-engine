package pricing.infrastructure.persistence.repository;

import pricing.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items")
    List<OrderEntity> findAllWithItems();
}
