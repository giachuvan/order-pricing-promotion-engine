package pricing.application.port;

import pricing.domain.model.Order;

import java.util.UUID;

public interface OrderPersistencePort {

    UUID save(Order order);
}
