package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AccumulationPeriodEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveBalanceUpdateType;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditOperationType;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditStatus;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveCreditTimingEnum;
import com.jojolaptech.camel.model.postgres.company.enums.LeaveTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "hrm_leave_credit")
public class LeaveCreditEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveTypeEnum leaveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private LeaveCreditOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status", nullable = false)
    private LeaveCreditStatus creditStatus;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "accumulation_period")
    private AccumulationPeriodEnum accumulationPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_timing")
    private LeaveCreditTimingEnum creditTiming;

    @Column(name = "accumulation_month")
    private Integer accumulationMonth;

    @Column(name = "fiscal_year_id")
    private UUID fiscalYearId;

    @Column(name = "system_generated", nullable = false)
    private Boolean systemGenerated;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_update_type")
    private LeaveBalanceUpdateType balanceUpdateType;
}
