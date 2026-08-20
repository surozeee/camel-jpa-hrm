package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.CalculatedHeadingType;
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
@Table(name = "calculatedTypeValue")
@Getter
@Setter
public class CalculatedTypeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculatedHeadingType", nullable = false)
    private CalculatedHeadingType calculatedHeadingType;

    @Column(name = "calculatedValue", nullable = false)
    private BigDecimal calculatedValue;

    @Column(name = "finalValue", nullable = false)
    private BigDecimal finalValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payPeriod_id", nullable = false)
    private PayPeriod payPeriod;
}
