package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "timezone_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_timezone_local_timezone_locale",
                columnNames = {"timezone_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimezoneLocalEntity extends BaseAuditEntity {

    /** Localized display label for the timezone (overrides master code when Accept-Language matches). */
    @Column(nullable = false, length = 200)
    private String name;

    /** Locale code this translation is written in (LanguageEnum name, e.g. NE). */
    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timezone_id", nullable = false)
    private TimezoneEntity masterTimezone;
}
