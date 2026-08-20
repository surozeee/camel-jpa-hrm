package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "modulePricingCriteria")
@Getter
@Setter
public class ModulePricingCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "noOfDays", nullable = true)
    private Integer noOfDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appModule_id", nullable = false)
    private ApplicationModule appModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payType_id", nullable = true)
    private PayType payType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costType_id", nullable = true)
    private CostType costType;
}
