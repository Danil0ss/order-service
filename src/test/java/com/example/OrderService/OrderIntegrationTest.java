package com.example.OrderService;

import com.example.OrderService.dto.*;
import com.example.OrderService.entity.Order;
import com.example.OrderService.entity.Status;
import com.example.OrderService.repository.ItemRepository;
import com.example.OrderService.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@AutoConfigureWireMock(port = 8081)
class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void start() {
        postgres.start();
    }

    @AfterAll
    static void stop() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderFlow() throws Exception {
        Long itemId = createItem("Test Item", 500);
        mockUser(99L);

        OrderRequestDTO orderRequest = new OrderRequestDTO(99L, List.of(new OrderItemDTO(itemId, 2)));
        String orderJson = objectMapper.writeValueAsString(orderRequest);

        mockMvc.perform(post("/api/orders")
                        .with(user("99").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(1000.0))
                .andExpect(jsonPath("$.user.email").value("integration@test.com"));
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Long itemId1 = createItem("Item 1", 100);
        Long itemId2 = createItem("Item 2", 200);
        mockUser(99L);

        OrderRequestDTO createRequest = new OrderRequestDTO(99L, List.of(new OrderItemDTO(itemId1, 1)));

        String createResponse = mockMvc.perform(post("/api/orders")
                        .with(user("99").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        OrderDTO createdOrder = objectMapper.readValue(createResponse, OrderDTO.class);

        OrderRequestDTO updateRequest = new OrderRequestDTO(99L, List.of(new OrderItemDTO(itemId2, 3)));

        mockMvc.perform(put("/api/orders/" + createdOrder.id())
                        .with(user("99").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(600.0));
    }

    @Test
    void shouldChangeStatus() throws Exception {
        Long itemId = createItem("Item", 100);
        mockUser(99L);

        OrderRequestDTO createRequest = new OrderRequestDTO(99L, List.of(new OrderItemDTO(itemId, 1)));

        String createResponse = mockMvc.perform(post("/api/orders")
                        .with(user("99").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        OrderDTO createdOrder = objectMapper.readValue(createResponse, OrderDTO.class);

        mockMvc.perform(patch("/api/orders/" + createdOrder.id() + "/status")
                        .with(user("99").roles("ADMIN"))
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isNoContent());

        Order orderInDb = orderRepository.findById(createdOrder.id()).orElseThrow();
        assertThat(orderInDb.getStatus()).isEqualTo(Status.PROCESSING);
    }

    @Test
    void shouldSoftDeleteOrder() throws Exception {
        Long itemId = createItem("Item", 100);
        mockUser(99L);

        OrderRequestDTO createRequest = new OrderRequestDTO(99L, List.of(new OrderItemDTO(itemId, 1)));

        String createResponse = mockMvc.perform(post("/api/orders")
                        .with(user("99").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        OrderDTO createdOrder = objectMapper.readValue(createResponse, OrderDTO.class);

        mockMvc.perform(delete("/api/orders/" + createdOrder.id())
                        .with(user("99").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Map<String, Object> orderMap = jdbcTemplate.queryForMap(
                "SELECT * FROM orders WHERE id = ?",
                createdOrder.id()
        );

        assertThat(orderMap).isNotNull();
        assertThat(orderMap.get("deleted")).isEqualTo(true);
    }

    @Test
    void shouldFilterOrders() throws Exception {
        Long itemId = createItem("Item", 100);
        mockUser(99L);

        OrderRequestDTO req = new OrderRequestDTO(99L, List.of(new OrderItemDTO(itemId, 1)));

        mockMvc.perform(post("/api/orders")
                .with(user("99").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        String res2 = mockMvc.perform(post("/api/orders")
                        .with(user("99").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        Long id2 = objectMapper.readValue(res2, OrderDTO.class).id();

        mockMvc.perform(patch("/api/orders/" + id2 + "/status")
                .with(user("99").roles("ADMIN"))
                .with(csrf())
                .param("status", "COMPLETED"));

        mockMvc.perform(get("/api/orders")
                        .with(user("99").roles("ADMIN"))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/orders")
                        .with(user("99").roles("ADMIN"))
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldReturn404_WhenOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/9999")
                        .with(user("99").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    private Long createItem(String name, double price) throws Exception {
        ProductDto product = new ProductDto(name, BigDecimal.valueOf(price));
        String itemJson = objectMapper.writeValueAsString(product);

        String itemResponse = mockMvc.perform(post("/api/items")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(itemResponse, ItemResponseDTO.class).id();
    }

    private void mockUser(Long userId) {
        stubFor(WireMock.get(urlEqualTo("/api/users/internal/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": %d,
                                    "email": "integration@test.com",
                                    "name": "Integration",
                                    "surname": "User"
                                }
                                """.formatted(userId))));
    }
}