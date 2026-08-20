package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PremiumFrequency;
import com.jojolaptech.camel.model.mysql.enums.TrainingFundedBy;
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
@Table(name = "employeeInsurance")
@Getter
@Setter
public class EmployeeInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "isHealthInsurance", nullable = true)
    private Boolean isHealthInsurance;

    @Column(name = "policyNumber", nullable = true)
    private String policyNumber;

    @Column(name = "financialYear", nullable = true)
    private String financialYear;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "policyStartDate", nullable = true)
    private Date policyStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "policyEndDate", nullable = true)
    private Date policyEndDate;

    @Column(name = "policyAmount", nullable = true)
    private Integer policyAmount;

    @Column(name = "periodicAmount", nullable = true)
    private String periodicAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "premiumFrequency", nullable = true)
    private PremiumFrequency premiumFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "premiumPaidBy", nullable = true)
    private TrainingFundedBy premiumPaidBy;

    @Column(name = "premiumAmount", nullable = true)
    private Integer premiumAmount;

    @Column(name = "notes", nullable = true)
    private String notes;

    @Column(name = "amountPaidByCompany", nullable = true)
    private Integer amountPaidByCompany;

    @Column(name = "amountPaidByEmployee", nullable = true)
    private Integer amountPaidByEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insuranceCompany_id", nullable = false)
    private InsuranceCompany insuranceCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
