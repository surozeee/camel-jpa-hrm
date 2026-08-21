package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "organization")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrganizationEntity extends BaseAuditEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String phone;

    private String email;

    private String website;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organization_type_id", nullable = false)
    private OrganizationTypeEntity organizationType;

    /** Required when organization type is OTHER. */
    @Column(name = "organization_type_other_specify")
    private String organizationTypeOtherSpecify;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organization_address_id")
    private OrganizationAddressEntity organizationAddress;

    @OneToMany(mappedBy = "organization", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<CompanyEntity> companies;
}
