package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "hrm_employee_temp_shift")
public class EmployeeTempShiftEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "branch_shift_id")
    private UUID branchShiftId;

    @Builder.Default
    @Column(name = "off_day", nullable = false)
    private Boolean offDay = false;

    @Column(length = 1000)
    private String remarks;
}
