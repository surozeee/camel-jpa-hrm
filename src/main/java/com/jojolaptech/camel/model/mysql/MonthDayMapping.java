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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "monthDayMapping")
@Getter
@Setter
public class MonthDayMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "monthNumber", nullable = false)
    private Integer monthNumber;

    @Column(name = "dayCount", nullable = false)
    private Integer dayCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDateAd", nullable = true)
    private Date startDateAd;

    @Column(name = "startDateBs", nullable = true)
    private String startDateBs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nyear_id", nullable = false)
    private NepaliYear nyear;
}
