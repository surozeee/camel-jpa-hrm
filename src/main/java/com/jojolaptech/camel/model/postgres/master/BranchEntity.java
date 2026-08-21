package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BranchEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    private String address;

    private String contactNumber;

    private String email;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "bank_id", nullable = false)
    private BankEntity bank;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "city_id")
    private CityEntity city;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "district_id")
    private DistrictEntity district;
}

