package pricing.application;

import pricing.application.command.CalculateOrderCommand;
import pricing.domain.pricing.PricingResult;

public interface OrderPricingUseCase {

    PricingResult calculate(CalculateOrderCommand command);
}
