package pricing.application;

import pricing.application.command.CalculateOrderCommand;
import pricing.application.command.OrderLineCommand;
import pricing.application.port.ActivePromotionPort;
import pricing.application.port.CouponResolutionPort;
import pricing.application.port.OrderPersistencePort;
import pricing.domain.model.Order;
import pricing.domain.model.OrderLine;
import pricing.domain.pricing.LineItem;
import pricing.domain.pricing.PricingContext;
import pricing.domain.pricing.PricingResult;
import pricing.domain.promotion.PromotionChain;
import pricing.domain.promotion.PromotionRule;
import pricing.domain.promotion.PromotionRuleFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderPricingService implements OrderPricingUseCase {

    private final ActivePromotionPort activePromotionPort;
    private final CouponResolutionPort couponResolutionPort;
    private final PromotionRuleFactory promotionRuleFactory;
    private final PromotionChain promotionChain;
    private final OrderPersistencePort orderPersistencePort;

    public OrderPricingService(
            ActivePromotionPort activePromotionPort,
            CouponResolutionPort couponResolutionPort,
            PromotionRuleFactory promotionRuleFactory,
            PromotionChain promotionChain,
            OrderPersistencePort orderPersistencePort
    ) {
        this.activePromotionPort = activePromotionPort;
        this.couponResolutionPort = couponResolutionPort;
        this.promotionRuleFactory = promotionRuleFactory;
        this.promotionChain = promotionChain;
        this.orderPersistencePort = orderPersistencePort;
    }

    @Override
    @Transactional
    public PricingResult calculate(CalculateOrderCommand command) {
        List<LineItem> lineItems = command.items().stream()
                .map(i -> new LineItem(i.sku(), i.price(), i.quantity()))
                .toList();

        BigDecimal subtotal = lineItems.stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String couponCode = normalizeCouponCode(command.couponCode());
        PricingContext context = new PricingContext(
                subtotal,
                lineItems,
                command.customerType(),
                couponCode
        );

        var activePromotions = activePromotionPort.findActivePromotions();
        var coupon = couponResolutionPort.resolve(couponCode);
        List<PromotionRule> rules = promotionRuleFactory.createRules(activePromotions, coupon);
        PricingResult result = promotionChain.apply(context, rules);

        orderPersistencePort.save(toOrder(command, lineItems, result, couponCode));
        return result;
    }

    private static String normalizeCouponCode(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        return couponCode.trim();
    }

    private static Order toOrder(
            CalculateOrderCommand command,
            List<LineItem> lineItems,
            PricingResult result,
            String couponCode
    ) {
        List<OrderLine> orderLines = lineItems.stream()
                .map(OrderPricingService::toOrderLine)
                .toList();

        return new Order(
                null,
                command.customerType(),
                couponCode,
                result.subtotal(),
                result.totalDiscount(),
                result.finalPrice(),
                orderLines
        );
    }

    private static OrderLine toOrderLine(LineItem item) {
        return new OrderLine(
                item.sku(),
                item.price(),
                item.quantity(),
                item.price().multiply(BigDecimal.valueOf(item.quantity()))
        );
    }
}
