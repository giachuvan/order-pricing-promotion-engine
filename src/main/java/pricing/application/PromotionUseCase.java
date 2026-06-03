package pricing.application;

import pricing.application.command.CreatePromotionCommand;
import pricing.domain.model.Promotion;

import java.util.List;

public interface PromotionUseCase {

    List<Promotion> listActive();

    Promotion create(CreatePromotionCommand command);
}
