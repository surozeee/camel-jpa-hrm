package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.FiscalYearTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.MonthTypeEnum;
import com.jojolaptech.camel.model.postgres.master.enums.MonthEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "fiscal_year_type")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FiscalYearTypeEntity extends BaseAuditEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private FiscalYearTypeEnum fiscalYearType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonthTypeEnum monthType;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_month")
    private MonthEnum month;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer displayOrder;

    @OneToMany(mappedBy = "fiscalYearType", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<FiscalYearEntity> fiscalYears;
}

