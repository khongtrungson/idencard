package com.idencard.auth.infrastructure.persistence.repository;

import com.idencard.auth.infrastructure.persistence.entity.ClientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataClientRepository extends JpaRepository<ClientJpaEntity, String> {
    Optional<ClientJpaEntity> findByClientId(String clientId);
    boolean existsByClientId(String clientId);
}
