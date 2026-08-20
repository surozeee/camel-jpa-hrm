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
@Table(name = "attDeviceMAC")
@Getter
@Setter
public class AttDeviceMAC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "macId", nullable = false)
    private String macId;

    @Column(name = "deviceSn", nullable = false)
    private String deviceSn;

    @Column(name = "deviceName", nullable = true)
    private String deviceName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastLogTime", nullable = true)
    private Date lastLogTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
