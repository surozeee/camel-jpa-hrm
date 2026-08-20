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
@Table(name = "taxation")
@Getter
@Setter
public class Taxation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "startRange", nullable = false)
    private double startRange;

    @Column(name = "endRange", nullable = false)
    private double endRange;

    @Column(name = "percent", nullable = false)
    private double percent;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isIncapacitated", nullable = false, length = 1)
    private Boolean isIncapacitated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscalYear_id", nullable = false)
    private FiscalYear fiscalYear;
}
