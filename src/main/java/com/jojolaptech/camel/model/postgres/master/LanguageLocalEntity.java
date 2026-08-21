package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "language_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_language_local_language_locale",
                columnNames = {"language_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LanguageLocalEntity extends BaseAuditEntity {

    /** Display name of the master language in the selected locale. */
    @Column(nullable = false, length = 200)
    private String name;

    /** Locale code this translation is written in (LanguageEnum name, e.g. NE). */
    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private LanguageEntity masterLanguage;
}
