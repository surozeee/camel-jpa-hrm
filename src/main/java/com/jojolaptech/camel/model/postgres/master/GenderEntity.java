package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gender")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GenderEntity extends BaseAuditEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String code;

    private String description;
}

