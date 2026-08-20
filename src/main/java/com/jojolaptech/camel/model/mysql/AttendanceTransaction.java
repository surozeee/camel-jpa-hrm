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
import java.sql.Time;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attendanceTransaction")
@Getter
@Setter
public class AttendanceTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "logDate", nullable = false)
    private Date logDate;

    @Column(name = "shiftInTime", nullable = true)
    private Time shiftInTime;

    @Column(name = "shiftOutTime", nullable = true)
    private Time shiftOutTime;

    @Column(name = "logInTime", nullable = true)
    private String logInTime;

    @Column(name = "logOutTime", nullable = true)
    private String logOutTime;

    @Column(name = "lateMinutes", nullable = false)
    private String lateMinutes;

    @Column(name = "earlyMinutes", nullable = false)
    private String earlyMinutes;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @Column(name = "overTime", nullable = false)
    private String overTime;

    @Column(name = "workTime", nullable = true)
    private String workTime = null;

    @Column(name = "underTime", nullable = true)
    private String underTime = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
