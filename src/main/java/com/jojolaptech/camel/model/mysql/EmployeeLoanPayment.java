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
@Table(name = "employeeLoanPayment")
@Getter
@Setter
public class EmployeeLoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "paidDate", nullable = false)
    private Date paidDate;

    @Column(name = "paidAmount", nullable = false)
    private BigDecimal paidAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeLoan_id", nullable = false)
    private EmployeeLoan employeeLoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payPeriod_id", nullable = false)
    private PayPeriod payPeriod;
}
