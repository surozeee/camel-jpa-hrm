package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.AccountDeleteStatusEnum;
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
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "secUserDeleteRequest")
@Getter
@Setter
public class SecUserDeleteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secUser_id", nullable = false)
    private SecUser secUser;

    @Column(name = "remarks", nullable = true)
    private String remarks;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "requestDate", nullable = false)
    private Date requestDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updateDate", nullable = true)
    private Date updateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountDeleteStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updatedBy_id", nullable = true)
    private SecUser updatedBy;
}
