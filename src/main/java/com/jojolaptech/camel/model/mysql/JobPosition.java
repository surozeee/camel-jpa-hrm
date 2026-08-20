package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.HireMethod;
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
@Table(name = "jobPosition")
@Getter
@Setter
public class JobPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "hireMethod", nullable = true)
    private HireMethod hireMethod;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "hireContractEndDate", nullable = true)
    private Date hireContractEndDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "appointmentHireDate", nullable = true)
    private Date appointmentHireDate;

    @Column(name = "appointmentLetterNo", nullable = true)
    private String appointmentLetterNo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "appointmentLetterDate", nullable = true)
    private Date appointmentLetterDate;

    @Column(name = "reference", nullable = true)
    private String reference;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "salaryCalculationDate", nullable = true)
    private Date salaryCalculationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
