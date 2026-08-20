package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.LeaveType;
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
@Table(name = "leaveApplication")
@Getter
@Setter
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "requestDate", nullable = false)
    private Date requestDate;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "leaveFrom", nullable = false)
    private Date leaveFrom;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "leaveTo", nullable = false)
    private Date leaveTo;

    @Column(name = "totalDays", nullable = false)
    private Double totalDays;

    @Column(name = "status", nullable = true)
    private String status;

    @Column(name = "adminRemarks", nullable = true)
    private String adminRemarks;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "checkedDate", nullable = true)
    private Date checkedDate;

    @Column(name = "discardRemark", nullable = true)
    private String discardRemark;

    @Enumerated(EnumType.STRING)
    @Column(name = "leaveType", nullable = false)
    private LeaveType leaveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addedByEmp_id", nullable = true)
    private Employee addedByEmp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedByEmp_id", nullable = true)
    private Employee approvedByEmp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendedByEmp_id", nullable = true)
    private Employee recommendedByEmp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addedByComp_id", nullable = true)
    private Company addedByComp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedByComp_id", nullable = true)
    private Company approvedByComp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendedByComp_id", nullable = true)
    private Company recommendedByComp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "substituteDay", nullable = true)
    private Date substituteDay;

    @Column(name = "isSubstitute", nullable = true)
    private Boolean isSubstitute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_id", nullable = false)
    private Leaves leave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
