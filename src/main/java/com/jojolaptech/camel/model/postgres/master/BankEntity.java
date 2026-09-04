package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "bank")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BankEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    /** e.g. Central Bank, Commercial Bank, Development Bank */
    private String bankType;

    private String swiftCode;

    private String address;

    private String contactNumber;

    private String email;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "country_id")
    private CountryEntity country;

    @OneToMany(mappedBy = "bank", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<BranchEntity> branches;
}

