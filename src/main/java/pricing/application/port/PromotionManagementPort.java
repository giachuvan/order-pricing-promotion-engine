package pricing.application.port;

import pricing.domain.model.Promotion;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionManagementPort {

    List<Promotion> findActivePromotions();

    boolean existsActiveByType(String type);

    Promotion create(String type, BigDecimal value, boolean active);
}
