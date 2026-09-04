package com.jojolaptech.camel.model.postgres.company;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jojolaptech.camel.model.postgres.company.enums.RateTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownLineTypeEnum;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownPercentBaseEnum;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Simplified payslip snapshot persisted as jsonb (compatible with ERP shape). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayrollCalculationSnapshot {

    private Integer snapshotVersion;
    private Boolean calculationDetailsComplete;
    private String engineVersion;
    private String clientRequestId;
    private Integer salaryYear;
    private Integer salaryMonth;
    private UUID employeeId;
    private String employeeCode;
    private BigDecimal basicSalary;
    private BigDecimal grossSalary;
    private BigDecimal totalAllowances;
    private BigDecimal taxableIncome;
    private BigDecimal annualTaxableIncome;
    private BigDecimal incomeTax;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private List<SnapshotLine> lines;
    private Boolean legacyComponentBackfill;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SnapshotLine {
        private UUID branchSalaryBreakdownId;
        private String lineName;
        private SalaryBreakdownLineTypeEnum lineType;
        private BigDecimal amount;
        private Integer displayOrder;
        private Boolean isBasicSalaryLine;
        private RateTypeEnum rateType;
        private BigDecimal rateValue;
        private SalaryBreakdownPercentBaseEnum percentBase;
        private String remarks;
        private String status;
    }
}
