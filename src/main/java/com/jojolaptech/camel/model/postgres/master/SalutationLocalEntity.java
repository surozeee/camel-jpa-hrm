package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "salutation_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_salutation_local_salutation_locale",
                columnNames = {"salutation_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalutationLocalEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salutation_id", nullable = false)
    private SalutationEntity masterSalutation;
}
