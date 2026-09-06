package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AddressTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shared ERP {@code address} table used by branch.address_id and employee addresses.
 * Must be a single entity — Hibernate rejects two @Entity mappings on the same table.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "address")
public class AddressEntity extends BaseAuditEntity {

    @Column(unique = true)
    private Long mysqlId;

    @Enumerated(EnumType.STRING)
    private AddressTypeEnum addressType;

    @Column(nullable = false)
    private String streetAddress;

    private String streetAddress2;

    private UUID city;

    /** Naming strategy maps cityName → city_name (avoid @Column(name="city_name") dual-bind). */
    private String cityName;

    private UUID countryId;

    private UUID stateId;

    private UUID districtId;

    private UUID localUnitId;

    /** Legacy display / free-text state name (branch addresses). */
    private String state;

    @Column(length = 64)
    private String ward;

    private String postalCode;

    @Enumerated(EnumType.STRING)
    private CountryEnum country;

    private String latitude;

    private String longitude;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    private UUID employeeId;

    @OneToMany(mappedBy = "branchAddress", fetch = FetchType.LAZY)
    private List<BranchEntity> branches;
}
