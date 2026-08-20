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
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employeePayrollHeadingPayment")
@Getter
@Setter
public class EmployeePayrollHeadingPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeePayrollPayment_id", nullable = false)
    private EmployeePayrollPayment employeePayrollPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payrollHeading_id", nullable = false)
    private PayrollSystemHeading payrollHeading;
}
