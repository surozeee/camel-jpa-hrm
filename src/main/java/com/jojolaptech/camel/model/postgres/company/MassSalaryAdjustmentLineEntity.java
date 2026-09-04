package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.MassSalaryValueTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "hrm_mass_salary_adjustment_line")
public class MassSalaryAdjustmentLineEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adjustment_id", nullable = false)
    private MassSalaryAdjustmentEntity adjustment;

    @Column(name = "branch_salary_breakdown_id", nullable = false)
    private UUID branchSalaryBreakdownId;

    @Column(name = "line_name", length = 200)
    private String lineName;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private MassSalaryValueTypeEnum valueType;

    @Column(name = "adjustment_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal adjustmentValue;
}
