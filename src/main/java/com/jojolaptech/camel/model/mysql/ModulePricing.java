package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "modulePricing")
@Getter
@Setter
public class ModulePricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "startNo", nullable = false)
    private Integer startNo;

    @Column(name = "endNo", nullable = false)
    private Integer endNo;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isActive", nullable = false, length = 1)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulePricingCriteria_id", nullable = false)
    private ModulePricingCriteria modulePricingCriteria;
}
