package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hrm_company_fiscal_year",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "master_fiscal_year_id"})
)
public class CompanyFiscalYearEntity extends BaseAuditEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "master_fiscal_year_id", nullable = false)
    private UUID masterFiscalYearId;

    @Column(nullable = false)
    private String fiscalYear;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;
}
