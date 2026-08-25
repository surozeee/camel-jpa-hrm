package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.company.enums.FyClosingParameterTypeEnum;
import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Per fiscal-year closing checklist item (legacy CompanyFiscalYearClosingParameter).
 * Rows are scoped to the <strong>closing</strong> company fiscal year (not the newly opened one).
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_company_fy_closing_parameter", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_fy_closing_param",
                columnNames = {"company_fiscal_year_id", "parameter_type", "leave_type_id"})
}, indexes = {
        @Index(name = "idx_fy_closing_param_company", columnList = "company_id,completed")
})
public class CompanyFiscalYearClosingParameterEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    /** Closing / closed company fiscal year. */
    @Column(name = "company_fiscal_year_id", nullable = false)
    private UUID companyFiscalYearId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parameter_type", nullable = false, length = 32)
    private FyClosingParameterTypeEnum parameterType;

    @Column(name = "leave_type_id")
    private UUID leaveTypeId;

    @Column(name = "completed", nullable = false)
    @Builder.Default
    private Boolean completed = Boolean.FALSE;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by_user_id")
    private UUID completedByUserId;

    @Column(name = "carried_employee_count")
    private Integer carriedEmployeeCount;

    @Column(name = "carried_days_total")
    private Double carriedDaysTotal;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
