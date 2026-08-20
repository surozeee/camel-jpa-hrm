package com.jojolaptech.camel.model.mysql;

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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "massIncrement")
@Getter
@Setter
public class MassIncrement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "incrementValue", nullable = false)
    private BigDecimal incrementValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "incrementDate", nullable = false)
    private Date incrementDate;

    @Column(name = "decrement", nullable = false)
    private Boolean decrement = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "valueType", nullable = false)
    private PayrollValueType valueType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobLevel_id", nullable = true)
    private JobLevel jobLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = true)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payrollHeading_id", nullable = false)
    private PayrollSystemHeading payrollHeading;
}
