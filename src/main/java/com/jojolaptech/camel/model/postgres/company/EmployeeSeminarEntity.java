package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.SeminarFundedByEnum;
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
@Table(name = "hrm_employee_seminar")
public class EmployeeSeminarEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 255)
    private String organizer;

    @Column(length = 255)
    private String place;

    @Column(length = 128)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "funded_by", length = 32)
    private SeminarFundedByEnum fundedBy;

    @Column(length = 500)
    private String remarks;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
}
