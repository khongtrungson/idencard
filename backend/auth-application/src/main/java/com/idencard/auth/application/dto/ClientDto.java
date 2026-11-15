package com.idencard.auth.application.dto;

import com.idencard.auth.domain.model.ClientApp;

import java.util.Set;

public record ClientDto(
        String clientId,
        String clientName,
        Set<String> redirectUris,
        Set<String> scopes,
        boolean requirePkce
) {
    public static ClientDto fromDomain(ClientApp clientApp) {
        return new ClientDto(
                clientApp.getClientId(),
                clientApp.getClientName(),
                clientApp.getRedirectUris(),
                clientApp.getScopes(),
                clientApp.isRequireProofKeyForCodeExchange()
        );
    }
}
