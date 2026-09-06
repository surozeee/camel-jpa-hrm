package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "city")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CityEntity extends BaseAuditEntity {

    private String name;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private DistrictEntity district;

}
