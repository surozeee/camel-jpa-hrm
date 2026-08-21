package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "time_zone")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TimezoneEntity extends BaseAuditEntity {

    /** TimezoneEnum name stored as plain string (e.g. ASIA_KATHMANDU). */
    @Column(unique = true, nullable = false, length = 128)
    private String name;

    private String code;

    @Column(name = "utc_offset")
    private String utcOffset;
}
