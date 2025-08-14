package com.vitasync.auth_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb",
    "jwt.secret=test-secret-key-for-testing-purposes-only",
    "jwt.expiration=86400000"
})
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void healthCheck_ShouldReturnOk() {
        webTestClient.get()
                .uri("/auth/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void serviceInfo_ShouldReturnServiceDetails() {
        webTestClient.get()
                .uri("/auth/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.serviceName").exists()
                .jsonPath("$.version").exists();
    }

    @Test
    void validateToken_WithoutToken_ShouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/auth/validate")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/auth/validate")
                .header("Authorization", "Bearer invalid_token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() {
        String invalidRequest = "{}";
        
        webTestClient.post()
                .uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void login_WithInvalidData_ShouldReturnBadRequest() {
        String invalidRequest = "{}";
        
        webTestClient.post()
                .uri("/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
