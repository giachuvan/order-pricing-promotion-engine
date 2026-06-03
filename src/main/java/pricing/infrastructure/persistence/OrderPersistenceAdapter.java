package pricing.infrastructure.persistence;

import pricing.application.port.OrderPersistencePort;
import pricing.domain.model.Order;
import pricing.domain.model.OrderLine;
import pricing.infrastructure.persistence.entity.OrderEntity;
import pricing.infrastructure.persistence.entity.OrderItemEntity;
import pricing.infrastructure.persistence.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderPersistenceAdapter implements OrderPersistencePort {

    private final OrderRepository orderRepository;

    public OrderPersistenceAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public UUID save(Order order) {
        OrderEntity entity = toEntity(order);
        return orderRepository.save(entity).getId();
    }

    private static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setCustomerType(order.customerType());
        entity.setCouponCode(order.couponCode());
        entity.setSubtotal(order.subtotal());
        entity.setTotalDiscount(order.totalDiscount());
        entity.setFinalPrice(order.finalPrice());

        for (OrderLine line : order.items()) {
            OrderItemEntity item = new OrderItemEntity();
            item.setSku(line.sku());
            item.setPrice(line.price());
            item.setQuantity(line.quantity());
            item.setLineTotal(line.lineTotal());
            entity.addItem(item);
        }
        return entity;
    }
}
