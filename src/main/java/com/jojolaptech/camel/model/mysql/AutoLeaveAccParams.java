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
@Table(name = "autoLeaveAccParams")
@Getter
@Setter
public class AutoLeaveAccParams {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "accType", nullable = false)
    private Boolean accType = false;

    @Column(name = "paramName", nullable = false)
    private String paramName;

    @Column(name = "paramValue", nullable = false)
    private String paramValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "paramDate", nullable = false)
    private Date paramDate;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_id", nullable = false)
    private Leaves leave;
}
