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
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employeePayrollPaymentSetting")
@Getter
@Setter
public class EmployeePayrollPaymentSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "filePath", nullable = true)
    private String filePath;

    @Column(name = "fileIdentity", nullable = true)
    private String fileIdentity;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMethod", nullable = false)
    private PaymentMethodEnum paymentMethod;

    @Column(name = "status", nullable = false)
    private Boolean status = Boolean.FALSE;

    @Column(name = "institutionIdentity", nullable = true)
    private String institutionIdentity;

    @Column(name = "institutionDetail", nullable = true)
    private String institutionDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = true)
    private Bank bank;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = true)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = true)
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payPeriod_id", nullable = true)
    private PayPeriod payPeriod;
}
