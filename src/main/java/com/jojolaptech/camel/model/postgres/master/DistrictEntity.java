package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "district")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DistrictEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String name;

    private String code;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "state_id")
    private StateEntity state;

    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<CityEntity> city;

    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<LocalUnitEntity> localUnits;
}
