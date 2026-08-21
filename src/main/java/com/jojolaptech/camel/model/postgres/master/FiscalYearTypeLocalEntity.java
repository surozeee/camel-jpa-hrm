package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "fiscal_year_type_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fiscal_year_type_local_fiscal_year_type_locale",
                columnNames = {"fiscal_year_type_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FiscalYearTypeLocalEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiscal_year_type_id", nullable = false)
    private FiscalYearTypeEntity masterFiscalYearType;
}
