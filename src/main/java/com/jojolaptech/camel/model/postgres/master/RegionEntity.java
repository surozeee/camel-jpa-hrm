package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "region")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RegionEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String name;

    private String code;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "region_country", joinColumns = @JoinColumn(name = "region_Id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "country_id", referencedColumnName = "id"))
    private List<CountryEntity> country;
}
