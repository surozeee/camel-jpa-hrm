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
@Table(name = "jobLevelPayrollValue")
@Getter
@Setter
public class JobLevelPayrollValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "payrollAmount", nullable = false)
    private BigDecimal payrollAmount;

    @Column(name = "payrollYearly", nullable = false)
    private BigDecimal payrollYearly;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = false)
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobLevelPayroll_id", nullable = false)
    private JobLevelPayroll jobLevelPayroll;
}
