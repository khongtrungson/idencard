package com.idencard.auth.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Domain entity representing an authenticated identity in IdenCard.
 */
public class User {
    private final UUID id;
    private final String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private boolean enabled;
    private final Set<Role> roles;
    private final Instant createdAt;
    private Instant updatedAt;

    public User(UUID id, String username, String passwordHash, String email, String fullName, boolean enabled, Set<Role> roles, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "User id cannot be null");
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.email = email;
        this.fullName = fullName;
        this.enabled = enabled;
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static User createNew(String username, String passwordHash, String email, String fullName) {
        Set<Role> defaultRoles = new HashSet<>();
        defaultRoles.add(Role.ROLE_USER);
        defaultRoles.add(Role.ROLE_CARD_HOLDER);
        return new User(UUID.randomUUID(), username, passwordHash, email, fullName, true, defaultRoles, Instant.now(), Instant.now());
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public boolean isEnabled() { return enabled; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "Password hash cannot be null");
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
        this.updatedAt = Instant.now();
    }

    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
            this.updatedAt = Instant.now();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }
}
