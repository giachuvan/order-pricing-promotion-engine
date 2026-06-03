package pricing.application;

import pricing.api.dto.CalculateOrderRequest;
import pricing.api.dto.OrderCalculationResponse;
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
public class OrderPricingService {

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

    @Transactional
    public OrderCalculationResponse calculate(CalculateOrderRequest request) {
        List<LineItem> lineItems = request.items().stream()
                .map(i -> new LineItem(i.sku(), i.price(), i.quantity()))
                .toList();

        BigDecimal subtotal = lineItems.stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String couponCode = normalizeCouponCode(request.couponCode());
        PricingContext context = new PricingContext(
                subtotal,
                lineItems,
                request.customerType(),
                couponCode
        );

        var activePromotions = activePromotionPort.findActivePromotions();
        var coupon = couponResolutionPort.resolve(couponCode);
        List<PromotionRule> rules = promotionRuleFactory.createRules(activePromotions, coupon);
        PricingResult result = promotionChain.apply(context, rules);

        orderPersistencePort.save(toOrder(request, lineItems, result, couponCode));
        return OrderCalculationResponse.from(result);
    }

    private static String normalizeCouponCode(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        return couponCode.trim();
    }

    private static Order toOrder(
            CalculateOrderRequest request,
            List<LineItem> lineItems,
            PricingResult result,
            String couponCode
    ) {
        List<OrderLine> orderLines = lineItems.stream()
                .map(item -> new OrderLine(
                        item.sku(),
                        item.price(),
                        item.quantity(),
                        item.price().multiply(BigDecimal.valueOf(item.quantity()))
                ))
                .toList();

        return new Order(
                null,
                request.customerType(),
                couponCode,
                result.subtotal(),
                result.totalDiscount(),
                result.finalPrice(),
                orderLines
        );
    }
}
