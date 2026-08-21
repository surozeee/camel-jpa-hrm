package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "master_lookup_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_master_lookup_local_lookup_locale",
                columnNames = {"master_lookup_id", "locale_language"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterLookupLocalEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_lookup_id", nullable = false)
    private MasterLookupEntity masterLookup;
}
