package com.vitasync.auth_service.service;

import com.vitasync.auth_service.dto.AuthResponse;
import com.vitasync.auth_service.dto.LoginRequest;
import com.vitasync.auth_service.dto.RegisterRequest;
import com.vitasync.auth_service.model.User;
import com.vitasync.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Authentication service for VitaSync platform
 * Handles user registration, login, and token management
 */
@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Register a new user in the VitaSync platform
     */
    public Mono<AuthResponse> register(RegisterRequest request) {
        return checkUserExists(request.getEmail(), request.getPhoneNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("User already exists with this email or phone number"));
                    }
                    return createNewUser(request);
                })
                .flatMap(userRepository::save)
                .flatMap(this::generateAuthResponse);
    }

    /**
     * Authenticate user login
     */
    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Login failed: user not found for email={}", request.getEmail());
                    return Mono.error(new RuntimeException("User not found"));
                }))
                .flatMap(user -> validatePassword(request.getPassword(), user))
                .flatMap(user -> updateLastLoginAndGenerateResponse(user));
    }

    /**
     * Refresh JWT token using refresh token
     */
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        if (!jwtTokenService.validateTokenFormat(refreshToken) || 
            !jwtTokenService.isRefreshToken(refreshToken)) {
            return Mono.error(new RuntimeException("Invalid refresh token"));
        }

        String email = jwtTokenService.extractUsername(refreshToken);
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    if (!jwtTokenService.validateToken(refreshToken, user)) {
                        return Mono.error(new RuntimeException("Invalid refresh token"));
                    }
                    return generateAuthResponse(user);
                });
    }

    /**
     * Validate user by JWT token
     */
    public Mono<User> validateToken(String token) {
        try {
            if (!jwtTokenService.validateTokenFormat(token)) {
                return Mono.error(new RuntimeException("Invalid token format"));
            }

            String email = jwtTokenService.extractUsername(token);
            return userRepository.findByEmail(email)
                    .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                    .flatMap(user -> {
                        if (!jwtTokenService.validateToken(token, user)) {
                            return Mono.error(new RuntimeException("Invalid token"));
                        }
                        return Mono.just(user);
                    });
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Token validation failed: " + e.getMessage()));
        }
    }

    /**
     * Get user profile by ID
     */
    public Mono<User> getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    /**
     * Update user verification status
     */
    public Mono<User> verifyUser(Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    user.setIsVerified(true);
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }

    /**
     * Check if user exists by email or phone number
     */
    private Mono<Boolean> checkUserExists(String email, String phoneNumber) {
        return userRepository.existsByEmail(email)
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.just(true);
                    }
                    return userRepository.existsByPhoneNumber(phoneNumber);
                });
    }

    /**
     * Create new user from registration request
     */
    private Mono<User> createNewUser(RegisterRequest request) {
        return Mono.fromCallable(() -> {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRole(request.getRole());
            user.setBloodType(request.getBloodType());
            user.setLocationLat(request.getLocationLat());
            user.setLocationLng(request.getLocationLng());
            user.setOrganizationId(request.getOrganizationId());
            user.setIsActive(true);
            
            // Auto-verify admins and staff, others need verification
            user.setIsVerified(user.isAdmin() || user.isBloodBankStaff() || user.isHospitalStaff());
            
            return user;
        });
    }

    /**
     * Validate password during login
     */
    private Mono<User> validatePassword(String rawPassword, User user) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .flatMap(matches -> {
                    if (!matches) {
                        log.warn("Login failed: invalid password for userId={}", user.getId());
                        return Mono.error(new RuntimeException("Invalid password"));
                    }
                    if (!user.getIsActive()) {
                        log.warn("Login blocked: account deactivated for userId={}", user.getId());
                        return Mono.error(new RuntimeException("Account is deactivated"));
                    }
                    return Mono.just(user);
                });
    }

    /**
     * Update last login and generate auth response
     */
    private Mono<AuthResponse> updateLastLoginAndGenerateResponse(User user) {
        user.updateLastLogin();
        return userRepository.save(user)
                .flatMap(this::generateAuthResponse);
    }

    /**
     * Generate authentication response with tokens and user info
     */
    private Mono<AuthResponse> generateAuthResponse(User user) {
        return Mono.fromCallable(() -> {
            String accessToken = jwtTokenService.generateToken(user);
            String refreshToken = jwtTokenService.generateRefreshToken(user);
            Long expiresIn = jwtTokenService.getExpirationTime();

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole(),
                    user.getIsVerified(),
                    user.getBloodType(),
                    user.getLastLogin()
            );

            return new AuthResponse(accessToken, refreshToken, expiresIn, userInfo);
        });
    }
}
