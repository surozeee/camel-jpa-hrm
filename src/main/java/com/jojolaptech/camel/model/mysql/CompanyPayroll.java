package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companyPayroll")
@Getter
@Setter
public class CompanyPayroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "mobaletId", nullable = false)
    private String mobaletId;

    @Column(name = "bankCode", nullable = false)
    private String bankCode;

    @Column(name = "branchCode", nullable = true)
    private String branchCode;

    @Column(name = "accountNumber", nullable = false)
    private String accountNumber;

    @Column(name = "accountName", nullable = false)
    private String accountName;
}
