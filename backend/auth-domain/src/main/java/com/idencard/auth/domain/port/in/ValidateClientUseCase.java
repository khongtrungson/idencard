package com.idencard.auth.domain.port.in;

import com.idencard.auth.domain.model.ClientApp;

public interface ValidateClientUseCase {
    ClientApp validate(String clientId);
}
