package com.idencard.auth.domain.port.out;

import com.idencard.auth.domain.model.ClientApp;
import java.util.Optional;

public interface ClientRepositoryPort {
    ClientApp save(ClientApp clientApp);
    Optional<ClientApp> findByClientId(String clientId);
    boolean existsByClientId(String clientId);
}
