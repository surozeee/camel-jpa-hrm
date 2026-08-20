package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.Type;
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
import java.sql.Time;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attendanceForgot")
@Getter
@Setter
public class AttendanceForgot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sendDate", nullable = false)
    private Date sendDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "checkInOutDate", nullable = false)
    private Date checkInOutDate;

    @Column(name = "time", nullable = false)
    private Time time;

    @Column(name = "reason", nullable = true)
    private String reason;

    @Column(name = "status", nullable = true)
    private String status;

    @Column(name = "remark", nullable = true)
    private String remark;

    @Column(name = "latitude", nullable = true)
    private String latitude;

    @Column(name = "longitude", nullable = true)
    private String longitude;

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
