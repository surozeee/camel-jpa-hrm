package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PaymentMethodEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "employeePayrollPayment")
@Getter
@Setter
public class EmployeePayrollPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "netAmount", nullable = false)
    private BigDecimal netAmount;

    @Column(name = "taxableAmount", nullable = false)
    private BigDecimal taxableAmount;

    @Column(name = "taxAmount", nullable = false)
    private BigDecimal taxAmount;

    @Column(name = "yearlyTaxableAmount", nullable = false)
    private BigDecimal yearlyTaxableAmount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "calculatedDate", nullable = false)
    private Date calculatedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "verifiedDate", nullable = true)
    private Date verifiedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "paidDate", nullable = true)
    private Date paidDate;

    @Column(name = "status", nullable = false)
    private Boolean status = Boolean.TRUE;

    @Column(name = "comment", nullable = true)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMethod", nullable = true)
    private PaymentMethodEnum paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymentPeriod_id", nullable = false)
    private PayPeriod paymentPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;
}
