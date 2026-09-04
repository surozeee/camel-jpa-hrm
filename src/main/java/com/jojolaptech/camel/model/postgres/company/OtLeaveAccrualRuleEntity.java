package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.OtLeaveAccrualSourceEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "hrm_ot_leave_accrual_rule",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id"}))
public class OtLeaveAccrualRuleEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "hours_equivalent_to_one_day", nullable = false, precision = 8, scale = 2)
    private BigDecimal hoursEquivalentToOneDay;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ot_source", nullable = false)
    private OtLeaveAccrualSourceEnum otSource;

    @Column(name = "carry_remainder_minutes", nullable = false)
    private Boolean carryRemainderMinutes;

    @Column(name = "max_leave_days", precision = 12, scale = 4)
    private BigDecimal maxLeaveDays;

    @Column(length = 500)
    private String remarks;
}
