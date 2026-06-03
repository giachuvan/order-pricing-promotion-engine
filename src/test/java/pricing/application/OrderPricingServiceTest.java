package pricing.application;

import pricing.api.ErrorCode;
import pricing.application.command.CalculateOrderCommand;
import pricing.application.command.OrderLineCommand;
import pricing.application.exception.PricingException;
import pricing.application.port.ActivePromotionPort;
import pricing.application.port.CouponResolutionPort;
import pricing.application.port.OrderPersistencePort;
import pricing.domain.model.CustomerType;
import pricing.domain.model.PromotionDefinition;
import pricing.domain.promotion.PromotionChain;
import pricing.domain.promotion.PromotionRuleFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPricingServiceTest {

    @Mock
    private ActivePromotionPort activePromotionPort;
    @Mock
    private CouponResolutionPort couponResolutionPort;
    @Mock
    private PromotionRuleFactory promotionRuleFactory;
    @Mock
    private PromotionChain promotionChain;
    @Mock
    private OrderPersistencePort orderPersistencePort;

    @InjectMocks
    private OrderPricingService orderPricingService;

    @Test
    void calculateDelegatesToChainAndPersists() {
        when(activePromotionPort.findActivePromotions()).thenReturn(
                List.of(new PromotionDefinition("PERCENTAGE_DISCOUNT", new BigDecimal("10"))));
        when(couponResolutionPort.resolve("SUMMER10")).thenReturn(Optional.empty());
        when(promotionRuleFactory.createRules(any(), any())).thenReturn(List.of());
        when(promotionChain.apply(any(), any())).thenReturn(
                new pricing.domain.pricing.PricingResult(
                        new BigDecimal("250"),
                        List.of(),
                        BigDecimal.ZERO,
                        new BigDecimal("250")
                ));
        when(orderPersistencePort.save(any())).thenReturn(UUID.randomUUID());

        var command = new CalculateOrderCommand(
                CustomerType.VIP,
                List.of(new OrderLineCommand("A100", new BigDecimal("100"), 2)),
                "SUMMER10"
        );

        var result = orderPricingService.calculate(command);

        assertEquals(new BigDecimal("250"), result.subtotal());
        verify(orderPersistencePort).save(any());
        verify(couponResolutionPort).resolve("SUMMER10");
    }

    @Test
    void calculateTrimsCouponCodeBeforeResolve() {
        when(activePromotionPort.findActivePromotions()).thenReturn(List.of());
        when(couponResolutionPort.resolve("SUMMER10")).thenReturn(Optional.empty());
        when(promotionRuleFactory.createRules(any(), any())).thenReturn(List.of());
        when(promotionChain.apply(any(), any())).thenReturn(
                new pricing.domain.pricing.PricingResult(
                        new BigDecimal("100"),
                        List.of(),
                        BigDecimal.ZERO,
                        new BigDecimal("100")
                ));
        when(orderPersistencePort.save(any())).thenReturn(UUID.randomUUID());

        var command = new CalculateOrderCommand(
                CustomerType.REGULAR,
                List.of(new OrderLineCommand("A100", new BigDecimal("100"), 1)),
                "SUMMER10 "
        );

        orderPricingService.calculate(command);

        verify(couponResolutionPort).resolve("SUMMER10");
    }

    @Test
    void throwsWhenCouponInvalidAndDoesNotPersist() {
        when(activePromotionPort.findActivePromotions()).thenReturn(List.of());
        when(couponResolutionPort.resolve("BADCODE"))
                .thenThrow(new PricingException(ErrorCode.COUPON_NOT_FOUND, "Coupon code BADCODE was not found"));

        var command = new CalculateOrderCommand(
                CustomerType.VIP,
                List.of(new OrderLineCommand("A100", new BigDecimal("100"), 2)),
                "BADCODE"
        );

        PricingException ex = assertThrows(
                PricingException.class,
                () -> orderPricingService.calculate(command));

        assertEquals(ErrorCode.COUPON_NOT_FOUND, ex.getErrorCode());
        verify(orderPersistencePort, never()).save(any());
        verify(promotionChain, never()).apply(any(), any());
    }
}
