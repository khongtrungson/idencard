package com.idencard.auth.domain.port.in;

import com.idencard.auth.domain.model.User;

public interface LoadUserByUsernameUseCase {
    User loadByUsername(String username);
}
