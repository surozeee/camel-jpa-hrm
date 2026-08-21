package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "address")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BranchAddressEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String streetAddress;

    @Column(nullable = true)
    private UUID city;

    @Column(name = "city_name")
    private String cityName;

    private UUID countryId;

    private UUID stateId;

    private UUID districtId;

    /** Legacy display / free-text state name. */
    private String state;

    @Enumerated(EnumType.STRING)
    private CountryEnum country;

    private String latitude;

    private String longitude;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "branchAddress", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<BranchEntity> branches;
}

