package com.vitasync.auth_service.dto;

import com.vitasync.auth_service.model.Role;
import java.time.LocalDateTime;

/**
 * Authentication response DTO containing JWT token and user info
 */
public class AuthResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private String refreshToken;
    private Long expiresIn;
    private UserInfo user;
    
    // Alias for compatibility
    public String getAccessToken() {
        return token;
    }

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, String refreshToken, Long expiresIn, UserInfo user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * Nested class for user information in auth response
     */
    public static class UserInfo {
        private Long id;
        private String email;
        private String phoneNumber;
        private String firstName;
        private String lastName;
        private Role role;
        private Boolean isVerified;
        private String bloodType;
        private LocalDateTime lastLogin;

        // Constructors
        public UserInfo() {}

        public UserInfo(Long id, String email, String phoneNumber, String firstName, 
                       String lastName, Role role, Boolean isVerified, String bloodType, 
                       LocalDateTime lastLogin) {
            this.id = id;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.isVerified = isVerified;
            this.bloodType = bloodType;
            this.lastLogin = lastLogin;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public Boolean getIsVerified() {
            return isVerified;
        }

        public void setIsVerified(Boolean isVerified) {
            this.isVerified = isVerified;
        }

        public String getBloodType() {
            return bloodType;
        }

        public void setBloodType(String bloodType) {
            this.bloodType = bloodType;
        }

        public LocalDateTime getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}
