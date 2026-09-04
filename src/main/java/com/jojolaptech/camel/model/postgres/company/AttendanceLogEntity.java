package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.ConsumedAsEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LogTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_attendance_log")
public class AttendanceLogEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "enroll_id")
    private String enrollId;

    @Column(name = "log_date_time", nullable = false)
    private LocalDateTime logDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_type")
    private LogTypeEnum logType;

    private String location;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(length = 1000)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private AttendanceEntity attendance;

    @Builder.Default
    @Column(nullable = false)
    private Boolean consumed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "consumed_as")
    private ConsumedAsEnum consumedAs;

    @Builder.Default
    @Column(nullable = false)
    private Boolean duplicate = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean outlier = false;
}
