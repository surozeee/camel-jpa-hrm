package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "date_time_format_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_date_time_format_local_format_locale",
                columnNames = {"date_time_format_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DateTimeFormatLocalEntity extends BaseAuditEntity {

    /** Localized display name for the date-time format. */
    @Column(nullable = false, length = 200)
    private String name;

    /** Locale code this translation is written in (LanguageEnum name, e.g. NE). */
    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "date_time_format_id", nullable = false)
    private DateTimeFormatEntity masterFormat;
}
