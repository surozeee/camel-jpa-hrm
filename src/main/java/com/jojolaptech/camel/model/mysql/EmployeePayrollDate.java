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
@Table(name = "employeePayrollDate")
@Getter
@Setter
public class EmployeePayrollDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = true)
    private Date endDate;

    @Column(name = "payrollAmount", nullable = false)
    private BigDecimal payrollAmount;

    @Column(name = "yearlyAmount", nullable = true)
    private BigDecimal yearlyAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeePayrollHeading_id", nullable = false)
    private EmployeePayrollHeading employeePayrollHeading;
}
