package com.vitasync.auth_service.service;

import com.vitasync.auth_service.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Custom Reactive UserDetailsService implementation for VitaSync
 * Loads user details from database for authentication
 */
@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepository.findByEmail(email)
                .cast(UserDetails.class)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with email: " + email)));
    }

    /**
     * Find user by phone number (alternative login method)
     */
    public Mono<UserDetails> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .cast(UserDetails.class)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with phone: " + phoneNumber)));
    }
}
