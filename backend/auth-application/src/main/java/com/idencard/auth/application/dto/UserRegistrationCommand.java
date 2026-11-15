package com.idencard.auth.application.dto;

public record UserRegistrationCommand(
        String username,
        String password,
        String email,
        String fullName
) {}
