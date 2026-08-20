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
@Table(name = "companyEmailSetting")
@Getter
@Setter
public class CompanyEmailSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "hostName", nullable = false)
    private String hostName;

    @Column(name = "hostPort", nullable = false)
    private int hostPort;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "enableLeaveEmailNotification", nullable = false)
    private boolean enableLeaveEmailNotification;

    @Column(name = "enableAttendanceEmailNotification", nullable = false)
    private boolean enableAttendanceEmailNotification;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "attendanceEmailLastSent", nullable = true)
    private Date attendanceEmailLastSent;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "attendanceEmailNextSend", nullable = true)
    private Date attendanceEmailNextSend;

    @Column(name = "monthlySendDate", nullable = true)
    private Integer monthlySendDate;

    @Column(name = "weeklySendDay", nullable = true)
    private String weeklySendDay;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "attendanceEmailSendDuration", nullable = false)
    private String attendanceEmailSendDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
