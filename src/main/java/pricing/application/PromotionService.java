package pricing.application;

import pricing.api.ErrorCode;
import pricing.application.command.CreatePromotionCommand;
import pricing.application.exception.PricingException;
import pricing.application.port.PromotionManagementPort;
import pricing.domain.model.Promotion;
import pricing.domain.model.PromotionType;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionService implements PromotionUseCase {

    private final PromotionManagementPort promotionManagementPort;

    public PromotionService(PromotionManagementPort promotionManagementPort) {
        this.promotionManagementPort = promotionManagementPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> listActive() {
        return promotionManagementPort.findActivePromotions();
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50)
    )
    @Transactional
    public Promotion create(CreatePromotionCommand command) {
        try {
            PromotionType.valueOf(command.type());
        } catch (IllegalArgumentException ex) {
            throw new PricingException(
                    ErrorCode.INVALID_PROMOTION_TYPE,
                    "Unknown promotion type: " + command.type());
        }

        if (Boolean.TRUE.equals(command.active())
                && promotionManagementPort.existsActiveByType(command.type())) {
            throw new PricingException(
                    ErrorCode.PROMOTION_CONFLICT,
                    "Active promotion of type " + command.type() + " already exists");
        }

        return promotionManagementPort.create(
                command.type(),
                command.value(),
                command.active()
        );
    }
}
