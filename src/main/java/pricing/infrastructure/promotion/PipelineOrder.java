package pricing.infrastructure.promotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Execution order in the promotion pipeline (lower values run first).
 * Default order: PERCENTAGE (10) → VIP (20) → COUPON (30) → BUY2 (40).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PipelineOrder {

    int value();
}
