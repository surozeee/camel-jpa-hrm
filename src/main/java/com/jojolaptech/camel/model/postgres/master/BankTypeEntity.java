package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_type")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BankTypeEntity extends BaseAuditEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;
}
