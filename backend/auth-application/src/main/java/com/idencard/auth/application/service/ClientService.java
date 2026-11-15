package com.idencard.auth.application.service;

import com.idencard.auth.domain.exception.DomainException;
import com.idencard.auth.domain.model.ClientApp;
import com.idencard.auth.domain.port.in.ValidateClientUseCase;
import com.idencard.auth.domain.port.out.ClientRepositoryPort;

import java.util.Objects;

public class ClientService implements ValidateClientUseCase {

    private final ClientRepositoryPort clientRepository;

    public ClientService(ClientRepositoryPort clientRepository) {
        this.clientRepository = Objects.requireNonNull(clientRepository, "ClientRepositoryPort must not be null");
    }

    @Override
    public ClientApp validate(String clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new DomainException("Client application not found with id: " + clientId));
    }
}
