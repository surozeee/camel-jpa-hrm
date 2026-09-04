package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "hrm_leave_balance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type_id"}))
public class LeaveBalanceEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "total_leaves")
    private Double totalLeaves;

    @Column(name = "used_leaves")
    private Double usedLeaves;

    @Column(name = "remaining_leaves")
    private Double remainingLeaves;

    @Column(name = "pending_leaves")
    private Double pendingLeaves;

    @Column(name = "opening_leaves")
    private Double openingLeaves;

    @Column(name = "opening_set_at")
    private LocalDateTime openingSetAt;

    @Column(name = "opening_fiscal_year_id")
    private UUID openingFiscalYearId;

    @Column(name = "opening_remarks", length = 500)
    private String openingRemarks;
}
