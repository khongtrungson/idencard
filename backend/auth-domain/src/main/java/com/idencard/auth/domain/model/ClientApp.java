package com.idencard.auth.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Domain entity representing an OAuth2 / OIDC Client Application (e.g. Android client).
 */
public class ClientApp {
    private final String clientId;
    private final String clientSecret; // May be null/empty for public clients (PKCE)
    private final String clientName;
    private final Set<String> authenticationMethods;
    private final Set<String> authorizationGrantTypes;
    private final Set<String> redirectUris;
    private final Set<String> postLogoutRedirectUris;
    private final Set<String> scopes;
    private final boolean requireProofKeyForCodeExchange; // PKCE
    private final Instant createdAt;

    public ClientApp(String clientId, String clientSecret, String clientName,
                     Set<String> authenticationMethods, Set<String> authorizationGrantTypes,
                     Set<String> redirectUris, Set<String> postLogoutRedirectUris,
                     Set<String> scopes, boolean requireProofKeyForCodeExchange,
                     Instant createdAt) {
        this.clientId = Objects.requireNonNull(clientId, "Client ID cannot be null");
        this.clientSecret = clientSecret;
        this.clientName = clientName != null ? clientName : clientId;
        this.authenticationMethods = authenticationMethods != null ? new HashSet<>(authenticationMethods) : new HashSet<>();
        this.authorizationGrantTypes = authorizationGrantTypes != null ? new HashSet<>(authorizationGrantTypes) : new HashSet<>();
        this.redirectUris = redirectUris != null ? new HashSet<>(redirectUris) : new HashSet<>();
        this.postLogoutRedirectUris = postLogoutRedirectUris != null ? new HashSet<>(postLogoutRedirectUris) : new HashSet<>();
        this.scopes = scopes != null ? new HashSet<>(scopes) : new HashSet<>();
        this.requireProofKeyForCodeExchange = requireProofKeyForCodeExchange;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static ClientApp createPublicClient(String clientId, String clientName, Set<String> redirectUris, Set<String> scopes) {
        Set<String> authMethods = new HashSet<>();
        authMethods.add("none");

        Set<String> grantTypes = new HashSet<>();
        grantTypes.add("authorization_code");
        grantTypes.add("refresh_token");

        return new ClientApp(clientId, null, clientName, authMethods, grantTypes,
                redirectUris, Collections.emptySet(), scopes, true, Instant.now());
    }

    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getClientName() { return clientName; }
    public Set<String> getAuthenticationMethods() { return Collections.unmodifiableSet(authenticationMethods); }
    public Set<String> getAuthorizationGrantTypes() { return Collections.unmodifiableSet(authorizationGrantTypes); }
    public Set<String> getRedirectUris() { return Collections.unmodifiableSet(redirectUris); }
    public Set<String> getPostLogoutRedirectUris() { return Collections.unmodifiableSet(postLogoutRedirectUris); }
    public Set<String> getScopes() { return Collections.unmodifiableSet(scopes); }
    public boolean isRequireProofKeyForCodeExchange() { return requireProofKeyForCodeExchange; }
    public Instant getCreatedAt() { return createdAt; }
}
