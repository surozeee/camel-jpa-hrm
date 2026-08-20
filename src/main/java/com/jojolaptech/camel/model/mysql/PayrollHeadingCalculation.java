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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payrollHeadingCalculation")
@Getter
@Setter
public class PayrollHeadingCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "calculationType", nullable = false)
    private String calculationType;

    @Column(name = "calculationValue", nullable = false)
    private BigDecimal calculationValue;

    @Column(name = "alternativeValue", nullable = false)
    private BigDecimal alternativeValue;

    @Column(name = "comparisionMethod", nullable = false)
    private String comparisionMethod;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = false)
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyPayrollHeading_id", nullable = false)
    private CompanyPayrollHeading companyPayrollHeading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyBranchPayrollHeading_id", nullable = false)
    private CompanyBranchPayrollHeading companyBranchPayrollHeading;
}
