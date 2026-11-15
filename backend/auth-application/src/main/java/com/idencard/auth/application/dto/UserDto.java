package com.idencard.auth.application.dto;

import com.idencard.auth.domain.model.Role;
import com.idencard.auth.domain.model.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserDto(
        UUID id,
        String username,
        String email,
        String fullName,
        boolean enabled,
        Set<String> roles,
        Instant createdAt
) {
    public static UserDto fromDomain(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}
