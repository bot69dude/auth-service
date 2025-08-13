package com.vitasync.auth_service.controller;

import com.vitasync.auth_service.dto.AuthResponse;
import com.vitasync.auth_service.dto.LoginRequest;
import com.vitasync.auth_service.dto.RegisterRequest;
import com.vitasync.auth_service.model.User;
import com.vitasync.auth_service.service.AuthService;
import com.vitasync.auth_service.service.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Authentication REST controller for VitaSync platform
 * Handles user registration, login, token validation, and user management
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthService authService, JwtTokenService jwtTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Register a new user
     * POST /auth/register
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(authResponse -> ResponseEntity.status(HttpStatus.CREATED).body(authResponse))
                .onErrorResume(this::handleError);
    }

    /**
     * Authenticate user login
     * POST /auth/login
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .onErrorResume(this::handleError);
    }

    /**
     * Refresh JWT access token
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return authService.refreshToken(refreshToken)
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .onErrorResume(this::handleError);
    }

    /**
     * Validate JWT token and get user info
     * GET /auth/validate
     */
    @GetMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtTokenService.extractTokenFromHeader(authHeader);
        
        if (token == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Missing or invalid token")));
        }

        return authService.validateToken(token)
                .map(user -> {
                    Map<String, Object> response = Map.of(
                            "valid", true,
                            "userId", user.getId(),
                            "email", user.getEmail(),
                            "role", user.getRole().name(),
                            "isVerified", user.getIsVerified()
                    );
                    return ResponseEntity.ok(response);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "Invalid token")));
    }

    /**
     * Get current user profile
     * GET /auth/profile
     */
    @GetMapping("/profile")
    public Mono<ResponseEntity<User>> getCurrentUserProfile(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtTokenService.extractTokenFromHeader(authHeader);
        
        if (token == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return authService.validateToken(token)
                .flatMap(user -> authService.getUserProfile(user.getId()))
                .map(user -> ResponseEntity.ok(user))
                .onErrorResume(this::handleError);
    }

    /**
     * Get user profile by ID (for inter-service communication)
     * GET /auth/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable Long userId) {
        return authService.getUserProfile(userId)
                .map(user -> ResponseEntity.ok(user))
                .onErrorResume(this::handleError);
    }

    /**
     * Verify user account
     * POST /auth/verify/{userId}
     */
    @PostMapping("/verify/{userId}")
    public Mono<ResponseEntity<Map<String, Object>>> verifyUser(@PathVariable Long userId) {
        return authService.verifyUser(userId)
                .map(user -> {
                    Map<String, Object> response = Map.of(
                            "success", true,
                            "message", "User verified successfully",
                            "userId", user.getId(),
                            "isVerified", user.getIsVerified()
                    );
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(this::handleError);
    }

    /**
     * Health check endpoint
     * GET /auth/health
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> healthCheck() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "VitaSync Auth Service",
                "timestamp", System.currentTimeMillis()
        )));
    }

    /**
     * Get service info
     * GET /auth/info
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> getServiceInfo() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "serviceName", "VitaSync Authentication Service",
                "version", "1.0.0",
                "description", "Microservice for user authentication in VitaSync blood logistics platform",
                "features", new String[]{
                        "User Registration",
                        "JWT Authentication",
                        "Role-based Authorization",
                        "Token Refresh",
                        "User Profile Management"
                }
        )));
    }

    /**
     * Handle errors and return appropriate HTTP responses
     */
    private <T> Mono<ResponseEntity<T>> handleError(Throwable error) {
        String message = error.getMessage();
        
        if (message.contains("already exists")) {
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
        } else if (message.contains("not found")) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } else if (message.contains("Invalid") || message.contains("password")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }
}
