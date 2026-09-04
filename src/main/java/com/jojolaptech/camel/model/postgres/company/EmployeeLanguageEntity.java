package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.LanguageProficiencyEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Table(name = "hrm_employee_language")
public class EmployeeLanguageEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private LanguageProficiencyEnum speaking;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private LanguageProficiencyEnum reading;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private LanguageProficiencyEnum writing;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private LanguageProficiencyEnum listening;

    @Column(length = 500)
    private String remarks;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
}
