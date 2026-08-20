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
@Table(name = "companyBranchPayrollHeading")
@Getter
@Setter
public class CompanyBranchPayrollHeading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "status", nullable = false)
    private Boolean status = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branchDepartment_id", nullable = false)
    private Branch branchDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyPayrollHeading_id", nullable = false)
    private CompanyPayrollHeading companyPayrollHeading;
}
