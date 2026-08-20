package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.LeaveAdjustmentType;
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
@Table(name = "leaveAdjustment")
@Getter
@Setter
public class LeaveAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "days", nullable = false)
    private Double days;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @Column(name = "leaveName", nullable = false)
    private String leaveName;

    @Enumerated(EnumType.STRING)
    @Column(name = "leaveAdjustmentType", nullable = false)
    private LeaveAdjustmentType leaveAdjustmentType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "adjustedDate", nullable = true)
    private Date adjustedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscalYear_id", nullable = false)
    private FiscalYear fiscalYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leaves_id", nullable = false)
    private Leaves leaves;
}
