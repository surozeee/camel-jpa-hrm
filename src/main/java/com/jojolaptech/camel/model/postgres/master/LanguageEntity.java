package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "language")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LanguageEntity extends BaseAuditEntity {
    @Column(unique = true)
    private String name;

    /** ISO / LanguageEnum name stored as plain string (e.g. EN, NE). */
    @Column(unique = true, length = 16, nullable = false)
    private String code;

    /** System default language flag. Only one row should be true. */
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
