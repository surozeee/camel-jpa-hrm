package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hrm_company_address")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CompanyAddressEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String streetAddress;

    private String streetAddress2;

    @Column(nullable = true)
    private UUID city;

    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CountryEnum country;

    private String latitude;

    private String longitude;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;
}

