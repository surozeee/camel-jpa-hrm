package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "gender_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_gender_local_gender_locale",
                columnNames = {"gender_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenderLocalEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gender_id", nullable = false)
    private GenderEntity masterGender;
}
