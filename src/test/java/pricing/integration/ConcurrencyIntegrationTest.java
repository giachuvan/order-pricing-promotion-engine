package pricing.integration;

import pricing.infrastructure.persistence.entity.PromotionEntity;
import pricing.infrastructure.persistence.repository.OrderRepository;
import pricing.infrastructure.persistence.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class ConcurrencyIntegrationTest {

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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanOrders() {
        orderRepository.deleteAll();
    }

    @Test
    void concurrentCreateActivePromotion_allowsOnlyOneWinner() throws Exception {
        promotionRepository.deleteAll();
        try {
            runConcurrentPromotionCreate();
        } finally {
            restoreSeededPromotions();
        }
    }

    private void runConcurrentPromotionCreate() throws Exception {
        String body = """
                {
                  "type": "PERCENTAGE_DISCOUNT",
                  "value": 10,
                  "active": true
                }
                """;

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<ResponseEntity<String>>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return postJson("/api/v1/promotions", body);
                }));
            }
            start.countDown();

            int created = 0;
            int conflict = 0;
            for (Future<ResponseEntity<String>> future : futures) {
                ResponseEntity<String> response = future.get();
                if (response.getStatusCode() == HttpStatus.CREATED) {
                    created++;
                } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST
                        && response.getBody() != null
                        && response.getBody().contains("PROMOTION_CONFLICT")) {
                    conflict++;
                } else {
                    throw new AssertionError("Unexpected response: " + response.getStatusCode() + " " + response.getBody());
                }
            }

            assertEquals(1, created);
            assertEquals(1, conflict);
            assertEquals(1, promotionRepository.findByActiveTrue().size());
        } finally {
            executor.shutdownNow();
        }
    }

    private void restoreSeededPromotions() {
        promotionRepository.deleteAll();
        savePromotion("PERCENTAGE_DISCOUNT", new BigDecimal("10"), true);
        savePromotion("BUY2_GET1_FREE", new BigDecimal("1"), true);
        savePromotion("VIP_DISCOUNT", new BigDecimal("5"), true);
    }

    private void savePromotion(String type, BigDecimal value, boolean active) {
        PromotionEntity entity = new PromotionEntity();
        entity.setType(type);
        entity.setValue(value);
        entity.setActive(active);
        entity.setCreatedAt(Instant.now());
        promotionRepository.save(entity);
    }

    @Test
    void concurrentCalculate_producesConsistentPricesAndIndependentOrders() throws Exception {
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

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<ResponseEntity<String>>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return postJson("/api/v1/orders/calculate", body);
                }));
            }
            start.countDown();

            for (Future<ResponseEntity<String>> future : futures) {
                ResponseEntity<String> response = future.get();
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertTrue(response.getBody() != null && response.getBody().contains("\"finalPrice\":102.5"));
            }

            assertEquals(threads, orderRepository.count());
        } finally {
            executor.shutdownNow();
        }
    }

    private ResponseEntity<String> postJson(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:" + port + path;
        return restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }
}
