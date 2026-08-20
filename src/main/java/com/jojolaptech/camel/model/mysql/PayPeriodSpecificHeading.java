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
@Table(name = "payPeriodSpecificHeading")
@Getter
@Setter
public class PayPeriodSpecificHeading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payPeriod_id", nullable = false)
    private PayPeriod payPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyPayrollHeading_id", nullable = false)
    private CompanyPayrollHeading companyPayrollHeading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscalYear_id", nullable = false)
    private FiscalYear fiscalYear;
}
