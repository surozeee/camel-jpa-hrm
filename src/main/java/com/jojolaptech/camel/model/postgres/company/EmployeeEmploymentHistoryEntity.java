package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.EmploymentHistorySourceEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmploymentMovementTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "hrm_employee_employment_history")
public class EmployeeEmploymentHistoryEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private EmploymentMovementTypeEnum movementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 24)
    @Builder.Default
    private EmploymentHistorySourceEnum source = EmploymentHistorySourceEnum.MANUAL;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "grade_name", length = 255)
    private String gradeName;

    @Column(name = "grade_code", length = 64)
    private String gradeCode;

    @Column(name = "designation_id")
    private UUID designationId;

    @Column(name = "designation_name", length = 255)
    private String designationName;

    @Column(name = "employment_type_id")
    private UUID employmentTypeId;

    @Column(name = "employment_type_name", length = 255)
    private String employmentTypeName;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(length = 500)
    private String remarks;
}
