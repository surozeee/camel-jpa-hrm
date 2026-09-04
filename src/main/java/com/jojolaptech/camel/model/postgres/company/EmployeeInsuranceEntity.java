package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeInsuranceStatusEnum;
import com.jojolaptech.camel.model.postgres.company.enums.EmployeeInsuranceTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.PremiumFrequencyEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "hrm_employee_insurance")
public class EmployeeInsuranceEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EmployeeInsuranceTypeEnum policyType;

    @Column(nullable = false, length = 128)
    private String policyNumber;

    @Column(nullable = false, length = 255)
    private String providerName;

    @Column(precision = 19, scale = 4)
    private BigDecimal coverageAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal premiumAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private PremiumFrequencyEnum premiumFrequency;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EmployeeInsuranceStatusEnum policyStatus;

    @Column(length = 500)
    private String remarks;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
}
