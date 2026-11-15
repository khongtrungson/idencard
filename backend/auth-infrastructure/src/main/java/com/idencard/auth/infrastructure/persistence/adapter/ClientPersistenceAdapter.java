package com.idencard.auth.infrastructure.persistence.adapter;

import com.idencard.auth.domain.model.ClientApp;
import com.idencard.auth.domain.port.out.ClientRepositoryPort;
import com.idencard.auth.infrastructure.persistence.entity.ClientJpaEntity;
import com.idencard.auth.infrastructure.persistence.repository.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClientPersistenceAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository clientRepository;

    public ClientPersistenceAdapter(SpringDataClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public ClientApp save(ClientApp clientApp) {
        ClientJpaEntity entity = ClientJpaEntity.fromDomain(clientApp);
        ClientJpaEntity saved = clientRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<ClientApp> findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId).map(ClientJpaEntity::toDomain);
    }

    @Override
    public boolean existsByClientId(String clientId) {
        return clientRepository.existsByClientId(clientId);
    }
}
