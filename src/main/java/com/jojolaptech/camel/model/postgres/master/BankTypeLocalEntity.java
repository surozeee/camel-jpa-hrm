package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "bank_type_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bank_type_local_bank_type_locale",
                columnNames = {"bank_type_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankTypeLocalEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_type_id", nullable = false)
    private BankTypeEntity masterBankType;
}
