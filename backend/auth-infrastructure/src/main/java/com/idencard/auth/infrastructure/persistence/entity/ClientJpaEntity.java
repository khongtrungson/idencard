package com.idencard.auth.infrastructure.persistence.entity;

import com.idencard.auth.domain.model.ClientApp;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "oauth2_clients")
public class ClientJpaEntity {

    @Id
    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "client_secret", length = 255)
    private String clientSecret;

    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth2_client_auth_methods", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "auth_method", nullable = false)
    private Set<String> authenticationMethods = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth2_client_grant_types", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "grant_type", nullable = false)
    private Set<String> authorizationGrantTypes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth2_client_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri", nullable = false, length = 500)
    private Set<String> redirectUris = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth2_client_post_logout_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "post_logout_uri", nullable = false, length = 500)
    private Set<String> postLogoutRedirectUris = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth2_client_scopes", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "scope", nullable = false, length = 100)
    private Set<String> scopes = new HashSet<>();

    @Column(name = "require_pkce", nullable = false)
    private boolean requireProofKeyForCodeExchange;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ClientJpaEntity() {}

    public ClientJpaEntity(String clientId, String clientSecret, String clientName,
                           Set<String> authenticationMethods, Set<String> authorizationGrantTypes,
                           Set<String> redirectUris, Set<String> postLogoutRedirectUris,
                           Set<String> scopes, boolean requireProofKeyForCodeExchange,
                           Instant createdAt) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientName = clientName;
        this.authenticationMethods = authenticationMethods != null ? authenticationMethods : new HashSet<>();
        this.authorizationGrantTypes = authorizationGrantTypes != null ? authorizationGrantTypes : new HashSet<>();
        this.redirectUris = redirectUris != null ? redirectUris : new HashSet<>();
        this.postLogoutRedirectUris = postLogoutRedirectUris != null ? postLogoutRedirectUris : new HashSet<>();
        this.scopes = scopes != null ? scopes : new HashSet<>();
        this.requireProofKeyForCodeExchange = requireProofKeyForCodeExchange;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static ClientJpaEntity fromDomain(ClientApp domain) {
        return new ClientJpaEntity(
                domain.getClientId(),
                domain.getClientSecret(),
                domain.getClientName(),
                domain.getAuthenticationMethods(),
                domain.getAuthorizationGrantTypes(),
                domain.getRedirectUris(),
                domain.getPostLogoutRedirectUris(),
                domain.getScopes(),
                domain.isRequireProofKeyForCodeExchange(),
                domain.getCreatedAt()
        );
    }

    public ClientApp toDomain() {
        return new ClientApp(
                this.clientId,
                this.clientSecret,
                this.clientName,
                this.authenticationMethods,
                this.authorizationGrantTypes,
                this.redirectUris,
                this.postLogoutRedirectUris,
                this.scopes,
                this.requireProofKeyForCodeExchange,
                this.createdAt
        );
    }

    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getClientName() { return clientName; }
    public Set<String> getAuthenticationMethods() { return authenticationMethods; }
    public Set<String> getAuthorizationGrantTypes() { return authorizationGrantTypes; }
    public Set<String> getRedirectUris() { return redirectUris; }
    public Set<String> getPostLogoutRedirectUris() { return postLogoutRedirectUris; }
    public Set<String> getScopes() { return scopes; }
    public boolean isRequireProofKeyForCodeExchange() { return requireProofKeyForCodeExchange; }
    public Instant getCreatedAt() { return createdAt; }
}
