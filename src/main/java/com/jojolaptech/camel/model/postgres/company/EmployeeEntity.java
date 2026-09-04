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
@Table(name = "employee")
public class EmployeeEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(nullable = false, unique = true)
    private String employeeCode;

    private String enrollId;

    @Column(nullable = false)
    private String firstName;

    private String middleName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    private LocalDate terminationDate;

    @Column(length = 500)
    private String notes;

    private UUID departmentId;

    private UUID branchId;

    @Column(name = "division_id")
    private UUID divisionId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "branch_shift_id")
    private UUID branchShiftId;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "designation_id")
    private UUID designationId;

    @Column(name = "is_department_head")
    @Builder.Default
    private Boolean isDepartmentHead = false;
}
