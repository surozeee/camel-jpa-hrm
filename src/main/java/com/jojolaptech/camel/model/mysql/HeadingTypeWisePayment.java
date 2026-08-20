package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PayrollHeadingType;
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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "headingTypeWisePayment")
@Getter
@Setter
public class HeadingTypeWisePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "payrollHeadingType", nullable = false)
    private PayrollHeadingType payrollHeadingType;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeePayrollPayment_id", nullable = false)
    private EmployeePayrollPayment employeePayrollPayment;
}
