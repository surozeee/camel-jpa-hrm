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
@Table(name = "insurance")
@Getter
@Setter
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "policy", nullable = false)
    private String policy;

    @Column(name = "type", nullable = false)
    private String type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "maturitydate", nullable = false)
    private Date maturitydate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issueddate", nullable = false)
    private Date issueddate;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
