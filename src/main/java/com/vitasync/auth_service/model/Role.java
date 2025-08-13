package com.vitasync.auth_service.model;

/**
 * Role enumeration for VitaSync platform
 * Defines different user types in the blood logistics system
 */
public enum Role {
    DONOR("ROLE_DONOR", "Blood Donor"),
    PATIENT("ROLE_PATIENT", "Patient requiring transfusions"),
    ADMIN("ROLE_ADMIN", "System Administrator"),
    BLOOD_BANK_STAFF("ROLE_BLOOD_BANK_STAFF", "Blood Bank Staff"),
    HOSPITAL_STAFF("ROLE_HOSPITAL_STAFF", "Hospital Staff"),
    NGO_COORDINATOR("ROLE_NGO_COORDINATOR", "NGO Coordinator"),
    MEDICAL_PROFESSIONAL("ROLE_MEDICAL_PROFESSIONAL", "Medical Professional");

    private final String authority;
    private final String description;

    Role(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }
}
