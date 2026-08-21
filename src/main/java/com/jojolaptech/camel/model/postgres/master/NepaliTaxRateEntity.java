package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.master.enums.TaxRateTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nepali_tax_rate")
public class NepaliTaxRateEntity extends BaseAuditEntity {

    @Column(name = "min_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 32)
    @Builder.Default
    private TaxRateTypeEnum rateType = TaxRateTypeEnum.PERCENTAGE;

    @Column(name = "tax_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nepali_tax_id", nullable = false)
    private NepaliTaxEntity nepaliTax;
}
