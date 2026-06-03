package pricing.integration;

import pricing.domain.model.CustomerType;
import pricing.infrastructure.persistence.entity.OrderEntity;
import pricing.infrastructure.persistence.entity.OrderItemEntity;
import pricing.infrastructure.persistence.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class OrderCalculateIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("pricing")
            .withUsername("pricing")
            .withPassword("pricing");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanOrders() {
        orderRepository.deleteAll();
    }

    @Test
    void calculateWithSeededPromotionsAndCoupon() throws Exception {
        String body = """
                {
                  "customerType": "VIP",
                  "items": [
                    { "sku": "A100", "price": 100, "quantity": 2 },
                    { "sku": "B200", "price": 50, "quantity": 1 }
                  ],
                  "couponCode": "SUMMER10"
                }
                """;

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.subtotal").value(250))
                .andExpect(jsonPath("$.data.totalDiscount").value(147.50))
                .andExpect(jsonPath("$.data.finalPrice").value(102.50))
                .andExpect(jsonPath("$.data.discounts.length()").value(4));

        List<OrderEntity> orders = orderRepository.findAllWithItems();
        assertEquals(1, orders.size());

        OrderEntity order = orders.get(0);
        assertNotNull(order.getId());
        assertEquals(CustomerType.VIP, order.getCustomerType());
        assertEquals("SUMMER10", order.getCouponCode());
        assertEquals(0, new BigDecimal("250").compareTo(order.getSubtotal()));
        assertEquals(0, new BigDecimal("147.50").compareTo(order.getTotalDiscount()));
        assertEquals(0, new BigDecimal("102.50").compareTo(order.getFinalPrice()));

        List<OrderItemEntity> items = order.getItems();
        assertEquals(2, items.size());

        Map<String, OrderItemEntity> bySku = items.stream()
                .collect(Collectors.toMap(OrderItemEntity::getSku, Function.identity()));

        assertEquals(0, new BigDecimal("200").compareTo(bySku.get("A100").getLineTotal()));
        assertEquals(2, bySku.get("A100").getQuantity());
        assertEquals(0, new BigDecimal("50").compareTo(bySku.get("B200").getLineTotal()));
    }

    @Test
    void calculateWithoutCoupon() throws Exception {
        String body = """
                {
                  "customerType": "VIP",
                  "items": [
                    { "sku": "A100", "price": 100, "quantity": 2 },
                    { "sku": "B200", "price": 50, "quantity": 1 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.subtotal").value(250))
                .andExpect(jsonPath("$.data.totalDiscount").value(137.50))
                .andExpect(jsonPath("$.data.finalPrice").value(112.50))
                .andExpect(jsonPath("$.data.discounts.length()").value(3));

        List<OrderEntity> orders = orderRepository.findAllWithItems();
        assertEquals(1, orders.size());

        OrderEntity order = orders.get(0);
        assertNull(order.getCouponCode());
        assertEquals(0, new BigDecimal("112.50").compareTo(order.getFinalPrice()));
    }
}
