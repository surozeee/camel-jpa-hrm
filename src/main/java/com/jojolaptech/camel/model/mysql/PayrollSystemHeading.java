package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.CalculatedHeadingType;
import com.jojolaptech.camel.model.mysql.enums.CalculationMethod;
import com.jojolaptech.camel.model.mysql.enums.PayrollHeadingType;
import com.jojolaptech.camel.model.mysql.enums.PayrollValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payrollSystemHeading")
@Getter
@Setter
public class PayrollSystemHeading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "headingName", nullable = false)
    private String headingName;

    @Column(name = "status", nullable = false)
    private Boolean status = Boolean.TRUE;

    @Column(name = "maxValue", nullable = true)
    private BigDecimal maxValue;

    @Column(name = "minValue", nullable = true)
    private BigDecimal minValue;

    @Column(name = "flatMaxAmount", nullable = true)
    private BigDecimal flatMaxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculationMethod", nullable = true)
    private CalculationMethod calculationMethod;

    @Column(name = "isCalculatedValue", nullable = true)
    private Boolean isCalculatedValue = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculatedHeadingType", nullable = true)
    private CalculatedHeadingType calculatedHeadingType;

    @Column(name = "sortingNo", nullable = true)
    private Integer sortingNo;

    @Column(name = "groupSortingNo", nullable = true)
    private Integer groupSortingNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "headingType", nullable = true)
    private PayrollHeadingType headingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "valueType", nullable = true)
    private PayrollValueType valueType;

    @Column(name = "beforeTax", nullable = true)
    private Boolean beforeTax = true;

    @Column(name = "regular", nullable = false)
    private Boolean regular = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payrollGroupHeading_id", nullable = true)
    private PayrollSystemHeading payrollGroupHeading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "systemParentHeading_id", nullable = true)
    private PayrollSystemHeading systemParentHeading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculatedOn_id", nullable = true)
    private PayrollSystemHeading calculatedOn;
}
