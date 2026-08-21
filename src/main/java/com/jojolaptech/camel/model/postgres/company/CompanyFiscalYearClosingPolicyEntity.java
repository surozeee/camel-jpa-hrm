package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.company.enums.FyClosingNonSelectedLeaveActionEnum;
import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Company-level FY closing leave/payroll settlement policy
 * (legacy AttParams {@code fiscal_year_termination_leave_balance} + extensible payroll flags).
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_company_fy_closing_policy", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fy_closing_policy_company", columnNames = "company_id")
})
public class CompanyFiscalYearClosingPolicyEntity extends BaseAuditEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    /**
     * Leave types eligible to carry forward at FY close.
     * Empty = fall back to accumulation-rule allowCarryForward behaviour only.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "hrm_company_fy_closing_policy_carry_leave",
            joinColumns = @JoinColumn(name = "policy_id"),
            indexes = @Index(name = "idx_fy_close_carry_leave", columnList = "leave_type_id"))
    @Column(name = "leave_type_id", nullable = false)
    @Builder.Default
    private Set<UUID> carryForwardLeaveTypeIds = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "non_selected_leave_action", nullable = false, length = 16)
    @Builder.Default
    private FyClosingNonSelectedLeaveActionEnum nonSelectedLeaveAction =
            FyClosingNonSelectedLeaveActionEnum.SKIP;

    /** Leave types flagged as encashment-eligible at FY boundary (payroll may consume later). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "hrm_company_fy_closing_policy_encash_leave",
            joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "leave_type_id", nullable = false)
    @Builder.Default
    private Set<UUID> encashmentLeaveTypeIds = new HashSet<>();

    @Column(name = "enable_encashment_at_close", nullable = false)
    @Builder.Default
    private Boolean enableEncashmentAtClose = Boolean.FALSE;

    /**
     * When true, employee termination should settle remaining leave per carry/encash policy
     * (settlement engine may consume this flag).
     */
    @Column(name = "settle_leave_on_termination", nullable = false)
    @Builder.Default
    private Boolean settleLeaveOnTermination = Boolean.FALSE;

    /** Auto-create pending checklist rows on the closing FY when seeding / closing. */
    @Column(name = "auto_seed_checklist", nullable = false)
    @Builder.Default
    private Boolean autoSeedChecklist = Boolean.TRUE;

    @Column(name = "remarks", length = 1000)
    private String remarks;
}
