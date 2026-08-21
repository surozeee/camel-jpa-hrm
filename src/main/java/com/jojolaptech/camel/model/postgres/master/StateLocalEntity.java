package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "state_local",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_state_local_state_locale",
                columnNames = {"state_id", "locale_language"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StateLocalEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "locale_language", nullable = false, length = 16)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "state_id", nullable = false)
    private StateEntity masterState;
}
