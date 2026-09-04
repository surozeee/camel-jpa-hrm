package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeHealthStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.HealthConditionEnum;
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
@Table(name = "hrm_employee_health")
public class EmployeeHealthEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", length = 24)
    private EmployeeHealthStatusEnum healthStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_condition", length = 24)
    private HealthConditionEnum healthCondition;

    @Column(nullable = false)
    @Builder.Default
    private Boolean diagnosed = false;

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

    @Column(length = 255)
    private String hospital;

    @Column(name = "doctor_name", length = 255)
    private String doctorName;

    @Column(name = "ongoing_treatment", nullable = false)
    @Builder.Default
    private Boolean ongoingTreatment = false;

    @Column(name = "hospital_address", length = 500)
    private String hospitalAddress;

    @Column(name = "doctor_number", length = 64)
    private String doctorNumber;

    @Column(length = 500)
    private String remarks;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
}
