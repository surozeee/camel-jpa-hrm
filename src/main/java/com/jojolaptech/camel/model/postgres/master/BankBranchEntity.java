package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

/** Bank branch master — must not share class/entity name with company.BranchEntity. */
@Entity(name = "BankBranchEntity")
@Table(name = "bank_branch")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BankBranchEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    private String address;

    private String contactNumber;

    private String email;

    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    private BankEntity bank;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private CityEntity city;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private DistrictEntity district;
}
