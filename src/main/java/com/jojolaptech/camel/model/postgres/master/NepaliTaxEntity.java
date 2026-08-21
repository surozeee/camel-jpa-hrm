package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.master.enums.TaxMaritalStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "nepali_tax",
        uniqueConstraints = @UniqueConstraint(columnNames = {"marital_status", "fiscal_year_id"})
)
public class NepaliTaxEntity extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", nullable = false, length = 32)
    private TaxMaritalStatusEnum maritalStatus;

    @Column(name = "fiscal_year_id", nullable = false)
    private UUID fiscalYearId;

    @Builder.Default
    @OneToMany(mappedBy = "nepaliTax", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NepaliTaxRateEntity> rates = new ArrayList<>();
}
