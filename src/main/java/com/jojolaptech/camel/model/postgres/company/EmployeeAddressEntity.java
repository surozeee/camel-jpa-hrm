package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AddressTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "address")
public class EmployeeAddressEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    private AddressTypeEnum addressType;

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    private String streetAddress2;

    private UUID city;

    private UUID stateId;

    private UUID districtId;

    private UUID localUnitId;

    @Column(length = 64)
    private String ward;

    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CountryEnum country;

    private String latitude;

    private String longitude;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
}
