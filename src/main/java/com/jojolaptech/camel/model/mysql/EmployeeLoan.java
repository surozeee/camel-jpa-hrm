package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.LoanPaymentType;
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
@Table(name = "employeeLoan")
@Getter
@Setter
public class EmployeeLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "loanProvider", nullable = false)
    private String loanProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentType", nullable = false)
    private LoanPaymentType paymentType;

    @Column(name = "loanAmount", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "paymentAmount", nullable = false)
    private BigDecimal paymentAmount;

    @Column(name = "remainingAmount", nullable = false)
    private BigDecimal remainingAmount;

    @Column(name = "paymentFrequency", nullable = false)
    private Integer paymentFrequency;

    @Column(name = "status", nullable = false)
    private Boolean status = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
