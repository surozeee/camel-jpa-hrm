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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "attLogs")
@Getter
@Setter
public class AttLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "enrollId", nullable = true)
    private Integer enrollId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "checkTime", nullable = true)
    private Date checkTime;

    @Column(name = "checkType", nullable = true)
    private String checkType;

    @Column(name = "verifyCode", nullable = true)
    private Integer verifyCode;

    @Column(name = "sensorId", nullable = true)
    private Integer sensorId;

    @Column(name = "workCode", nullable = true)
    private Integer workCode;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isDevice", nullable = true, length = 1)
    private Boolean isDevice;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isAdd", nullable = true, length = 1)
    private Boolean isAdd;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isModified", nullable = true, length = 1)
    private Boolean isModified;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isDeleted", nullable = true, length = 1)
    private Boolean isDeleted;

    @Column(name = "modifiedBy", nullable = true)
    private String modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifiedDate", nullable = true)
    private Date modifiedDate;

    @Column(name = "deviceLogId", nullable = true)
    private Long deviceLogId;

    @Column(name = "macId", nullable = true)
    private String macId;

    @Column(name = "browserName", nullable = true)
    private String browserName;

    @Column(name = "os", nullable = true)
    private String os;

    @Column(name = "ip", nullable = true)
    private String ip;

    @Column(name = "timeZoneName", nullable = true)
    private String timeZoneName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
