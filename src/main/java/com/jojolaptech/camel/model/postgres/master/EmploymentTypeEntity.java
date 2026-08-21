package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employment_type")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmploymentTypeEntity extends BaseAuditEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 64)
    private String code;

    private String description;
}
