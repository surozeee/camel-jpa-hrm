package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

/** Experience-level master — must not share class/entity name with company.ExperienceEntity. */
@Entity(name = "MasterExperienceEntity")
@Table(name = "experience")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MasterExperienceEntity extends BaseAuditEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String code;

    private String description;
}
