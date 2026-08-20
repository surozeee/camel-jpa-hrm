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
import java.sql.Time;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attTimeTable")
@Getter
@Setter
public class AttTimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "onTime", nullable = true)
    private Time onTime;

    @Column(name = "offTime", nullable = true)
    private Time offTime;

    @Column(name = "startIn", nullable = true)
    private Time startIn;

    @Column(name = "stopIn", nullable = true)
    private Time stopIn;

    @Column(name = "startOut", nullable = true)
    private Time startOut;

    @Column(name = "stopOut", nullable = true)
    private Time stopOut;

    @Column(name = "lateIn", nullable = false)
    private Integer lateIn;

    @Column(name = "earlyOut", nullable = false)
    private Integer earlyOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
