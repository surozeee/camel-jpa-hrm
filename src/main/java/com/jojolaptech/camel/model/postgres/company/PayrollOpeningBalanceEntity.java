package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@Table(name = "hrm_payroll_opening_balance")
public class PayrollOpeningBalanceEntity extends BaseAuditEntity {

    /** fiscalYear.mysql id used as cutover key */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "company_fiscal_year_id", nullable = false)
    private UUID companyFiscalYearId;

    @Column(name = "months_already_processed", nullable = false)
    private Integer monthsAlreadyProcessed;

    @Column(name = "cutover_date")
    private LocalDate cutoverDate;

    @Column(length = 500)
    private String remarks;

    @Column(name = "locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @OneToMany(mappedBy = "openingBalance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PayrollOpeningBalanceLineEntity> lines = new ArrayList<>();
}
