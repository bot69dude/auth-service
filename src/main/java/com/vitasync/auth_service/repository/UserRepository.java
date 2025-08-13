package com.vitasync.auth_service.repository;

import com.vitasync.auth_service.model.Role;
import com.vitasync.auth_service.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for User entity using R2DBC
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    
    /**
     * Find user by email address
     */
    Mono<User> findByEmail(String email);
    
    /**
     * Find user by phone number
     */
    Mono<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * Check if user exists by email
     */
    Mono<Boolean> existsByEmail(String email);
    
    /**
     * Check if user exists by phone number
     */
    Mono<Boolean> existsByPhoneNumber(String phoneNumber);
    
    /**
     * Find all users by role
     */
    Flux<User> findByRole(Role role);
    
    /**
     * Find all active users
     */
    Flux<User> findByIsActiveTrue();
    
    /**
     * Find all verified users
     */
    Flux<User> findByIsVerifiedTrue();
    
    /**
     * Find users by blood type (for donors and patients)
     */
    Flux<User> findByBloodType(String bloodType);
    
    /**
     * Find users by role and blood type
     */
    Flux<User> findByRoleAndBloodType(Role role, String bloodType);
    
    /**
     * Find donors within a certain radius using Haversine formula
     * This query finds donors within specified distance from a location
     */
    @Query("""
        SELECT * FROM users 
        WHERE role = 'DONOR' 
        AND is_active = true 
        AND is_verified = true
        AND location_lat IS NOT NULL 
        AND location_lng IS NOT NULL
        AND (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(location_lat)) * 
                cos(radians(location_lng) - radians(:lng)) + 
                sin(radians(:lat)) * sin(radians(location_lat))
            )
        ) <= :radiusKm
        """)
    Flux<User> findDonorsWithinRadius(Double lat, Double lng, Double radiusKm);
    
    /**
     * Find compatible donors for a blood type within radius
     */
    @Query("""
        SELECT * FROM users 
        WHERE role = 'DONOR' 
        AND is_active = true 
        AND is_verified = true
        AND blood_type IN (:compatibleBloodTypes)
        AND location_lat IS NOT NULL 
        AND location_lng IS NOT NULL
        AND (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(location_lat)) * 
                cos(radians(location_lng) - radians(:lng)) + 
                sin(radians(:lat)) * sin(radians(location_lat))
            )
        ) <= :radiusKm
        ORDER BY (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(location_lat)) * 
                cos(radians(location_lng) - radians(:lng)) + 
                sin(radians(:lat)) * sin(radians(location_lat))
            )
        ) ASC
        """)
    Flux<User> findCompatibleDonorsWithinRadius(Double lat, Double lng, 
                                               Double radiusKm, 
                                               String[] compatibleBloodTypes);
    
    /**
     * Find users by organization ID (for hospital staff, NGO coordinators)
     */
    Flux<User> findByOrganizationId(Long organizationId);
    
    /**
     * Update last login timestamp
     */
    @Query("UPDATE users SET last_login = NOW(), updated_at = NOW() WHERE id = :userId")
    Mono<Void> updateLastLogin(Long userId);
}
