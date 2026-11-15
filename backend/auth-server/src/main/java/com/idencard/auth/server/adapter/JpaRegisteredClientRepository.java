package com.idencard.auth.server.adapter;

import com.idencard.auth.domain.model.ClientApp;
import com.idencard.auth.domain.port.out.ClientRepositoryPort;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientRepositoryPort clientRepository;

    public JpaRegisteredClientRepository(ClientRepositoryPort clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        // If needed to save from spring authorization server directly
    }

    @Override
    public RegisteredClient findById(String id) {
        return findByClientId(id);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient toRegisteredClient(ClientApp app) {
        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.nameUUIDFromBytes(app.getClientId().getBytes()).toString())
                .clientId(app.getClientId())
                .clientName(app.getClientName())
                .clientIdIssuedAt(app.getCreatedAt());

        if (app.getClientSecret() != null && !app.getClientSecret().isBlank()) {
            builder.clientSecret(app.getClientSecret());
        }

        // Authentication Methods
        for (String method : app.getAuthenticationMethods()) {
            builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method));
        }

        // Grant Types
        for (String grantType : app.getAuthorizationGrantTypes()) {
            builder.authorizationGrantType(new AuthorizationGrantType(grantType));
        }

        // Redirect URIs
        for (String redirectUri : app.getRedirectUris()) {
            builder.redirectUri(redirectUri);
        }

        // Post Logout Redirect URIs
        for (String postLogoutUri : app.getPostLogoutRedirectUris()) {
            builder.postLogoutRedirectUri(postLogoutUri);
        }

        // Scopes
        for (String scope : app.getScopes()) {
            builder.scope(scope);
        }

        // Client & Token Settings (PKCE enabled)
        builder.clientSettings(ClientSettings.builder()
                .requireProofKey(app.isRequireProofKeyForCodeExchange())
                .requireAuthorizationConsent(false)
                .build());

        builder.tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(2))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(true)
                .build());

        return builder.build();
    }
}
