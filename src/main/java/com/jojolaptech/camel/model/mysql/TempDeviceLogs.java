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
@Table(name = "tempDeviceLogs")
@Getter
@Setter
public class TempDeviceLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "enrollId", nullable = false)
    private Integer enrollId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "checkTime", nullable = false)
    private Date checkTime;

    @Column(name = "checkType", nullable = false)
    private String checkType;

    @Column(name = "verifyCode", nullable = false)
    private Integer verifyCode;

    @Column(name = "sensorId", nullable = false)
    private Integer sensorId;

    @Column(name = "workCode", nullable = false)
    private Integer workCode;

    @Column(name = "macId", nullable = true)
    private String macId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
