package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.TaxationValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "taxationRule")
@Getter
@Setter
public class TaxationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = true)
    private Date endDate;

    @Column(name = "rangeStart", nullable = false)
    private BigDecimal rangeStart;

    @Column(name = "rangeEnd", nullable = false)
    private BigDecimal rangeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "valueType", nullable = false)
    private TaxationValueType valueType;

    @Column(name = "taxValue", nullable = false)
    private BigDecimal taxValue;

    @Column(name = "isMarried", nullable = false)
    private Boolean isMarried = Boolean.TRUE;

    @Column(name = "isResident", nullable = false)
    private Boolean isResident = Boolean.TRUE;

    @Column(name = "isDifferentlyEnabled", nullable = false)
    private Boolean isDifferentlyEnabled = Boolean.TRUE;

    @Column(name = "status", nullable = false)
    private Boolean status = Boolean.TRUE;
}
