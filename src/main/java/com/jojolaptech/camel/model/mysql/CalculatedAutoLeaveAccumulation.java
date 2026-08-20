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
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "calculatedAutoLeaveAccumulation")
@Getter
@Setter
public class CalculatedAutoLeaveAccumulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdDate", nullable = false)
    private Date createdDate;

    @Column(name = "leaveValue", nullable = false)
    private Double leaveValue;

    @Column(name = "calYear", nullable = false)
    private Integer calYear;

    @Column(name = "calMonth", nullable = false)
    private Integer calMonth;

    @Column(name = "calculatedValue", nullable = false)
    private Double calculatedValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscalYear_id", nullable = false)
    private FiscalYear fiscalYear;

    @Column(name = "totalLeave", nullable = false)
    private Double totalLeave;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_id", nullable = false)
    private Leaves leave;
}
