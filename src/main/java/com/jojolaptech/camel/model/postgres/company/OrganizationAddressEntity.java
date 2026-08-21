package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organization_address")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrganizationAddressEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String streetAddress;

    private String streetAddress2;

    @Column(nullable = false)
    private UUID city;

    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CountryEnum country;

    private String latitude;

    private String longitude;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "organizationAddress", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<OrganizationEntity> organizations;
}

