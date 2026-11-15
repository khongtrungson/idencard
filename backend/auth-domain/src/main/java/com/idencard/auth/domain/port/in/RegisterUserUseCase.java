package com.idencard.auth.domain.port.in;

import com.idencard.auth.domain.model.User;

public interface RegisterUserUseCase {
    User register(String username, String rawPassword, String email, String fullName);
}
