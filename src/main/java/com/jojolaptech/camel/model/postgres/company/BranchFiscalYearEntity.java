package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.FiscalYearTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.MonthEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_branch_fiscal_year")
public class BranchFiscalYearEntity extends BaseAuditEntity {
    
    @Column(nullable = false, unique = true)
    private String fiscalYear;

    @Enumerated(EnumType.STRING)
    private MonthEnum month;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private FiscalYearTypeEnum fiscalYearType;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
}

