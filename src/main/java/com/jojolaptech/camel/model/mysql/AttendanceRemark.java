package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "attendanceRemark")
@Getter
@Setter
public class AttendanceRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "applicationDate", nullable = false)
    private Date applicationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "remarkDate", nullable = false)
    private Date remarkDate;

    @Column(name = "remark", nullable = false)
    private String remark;

    @Column(name = "status", nullable = true)
    private String status;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isDoNotCountLateMin", nullable = false, length = 1)
    private Boolean isDoNotCountLateMin = false;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isDoNotCountEarlyMin", nullable = false, length = 1)
    private Boolean isDoNotCountEarlyMin = false;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
