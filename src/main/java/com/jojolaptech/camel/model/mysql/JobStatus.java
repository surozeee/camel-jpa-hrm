package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.JobStatusStatus;
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
@Table(name = "jobStatus")
@Getter
@Setter
public class JobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fromDate", nullable = false)
    private Date fromDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "toDate", nullable = false)
    private Date toDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "jobStatusStatus", nullable = false)
    private JobStatusStatus jobStatusStatus;

    @Column(name = "noteText", nullable = false)
    private String noteText;

    @Column(name = "isActive", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
