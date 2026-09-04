package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "hrm_ot_leave_accrual_line")
public class OtLeaveAccrualLineEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private OtLeaveAccrualRunEntity run;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_code", length = 64)
    private String employeeCode;

    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @Column(name = "leave_days", nullable = false, precision = 12, scale = 4)
    private BigDecimal leaveDays;

    @Column(name = "remainder_minutes")
    private Integer remainderMinutes;

    @Column(name = "ot_minutes")
    private Integer otMinutes;

    @Column(name = "net_minutes")
    private Integer netMinutes;

    @Column(name = "undertime_minutes")
    private Integer undertimeMinutes;

    @Column(name = "previous_remainder_minutes")
    private Integer previousRemainderMinutes;

    @Column(name = "leave_credit_id")
    private UUID leaveCreditId;
}
