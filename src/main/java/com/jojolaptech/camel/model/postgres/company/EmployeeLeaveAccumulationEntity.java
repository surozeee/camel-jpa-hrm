package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "leave_accumulation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mysql_id"}))
public class EmployeeLeaveAccumulationEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true, nullable = false)
    private Long mysqlId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    /** Fiscal period bucket as YYYYMM (e.g. 202605). */
    @Column(name = "accumulation_month", nullable = false)
    private Integer accumulationMonth;

    @Column(name = "accumulated_leaves", nullable = false, precision = 8, scale = 2)
    private BigDecimal accumulatedLeaves;

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_leave_type_id", nullable = false)
    private BranchLeaveTypeEntity branchLeaveType;
}
