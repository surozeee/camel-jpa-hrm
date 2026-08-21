package com.jojolaptech.camel.model.postgres.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "token")
public class TokenEntity {

    @Id
    @Column
    private String id;
    private String principalName;
    private String registeredClientId;
    private String authorizationGrantType;

    @Column(columnDefinition = "TEXT")
    private String authorizedScopes;

    @Column(columnDefinition = "TEXT")
    private String attributes;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String accessTokenValue;

    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String accessTokenMetadata;

    @Column(columnDefinition = "TEXT")
    private String refreshTokenValue;

    private Instant refreshTokenIssuedAt;
    private Instant refreshTokenExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String refreshTokenMetadata;

}
