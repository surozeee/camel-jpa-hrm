package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.CurrencyEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currency")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CurrencyEntity extends BaseAuditEntity {

    @Column(unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private CurrencyEnum code;

}
