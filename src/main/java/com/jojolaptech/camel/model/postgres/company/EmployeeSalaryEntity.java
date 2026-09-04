package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
        name = "hrm_employee_salary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "effective_date"}))
public class EmployeeSalaryEntity extends BaseAuditEntity {

    /** employee.mysql_id for the open migration revision */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 500)
    private String remarks;

    @OneToMany(mappedBy = "employeeSalary", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeSalaryComponentEntity> components = new ArrayList<>();
}
