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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "privilageEmployeeEmailSetting")
@Getter
@Setter
public class PrivilageEmployeeEmailSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "leaveApply", nullable = false)
    private boolean leaveApply;

    @Column(name = "leaveRecommend", nullable = false)
    private boolean leaveRecommend;

    @Column(name = "leaveApprove", nullable = false)
    private boolean leaveApprove;

    @Column(name = "checkinApply", nullable = false)
    private boolean checkinApply;

    @Column(name = "checkinRecommend", nullable = false)
    private boolean checkinRecommend;

    @Column(name = "checkInApprove", nullable = false)
    private boolean checkInApprove;

    @Column(name = "remarkApply", nullable = false)
    private boolean remarkApply;

    @Column(name = "remarkRecommend", nullable = false)
    private boolean remarkRecommend;

    @Column(name = "remarkApprove", nullable = false)
    private boolean remarkApprove;

    @Column(name = "sendAttendanceLogEmail", nullable = false)
    private boolean sendAttendanceLogEmail;

    @Column(name = "departmentLeavelFilter", nullable = false)
    private boolean departmentLeavelFilter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
