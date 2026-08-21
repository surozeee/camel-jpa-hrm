package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "country_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_country_local_country_locale",
                columnNames = {"country_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountryLocalEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private CountryEntity masterCountry;
}
