package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.JobLevel;
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
@Table(name = "jobDescription")
@Getter
@Setter
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effectiveDate", nullable = true)
    private Date effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "jobLevel", nullable = true)
    private JobLevel jobLevel;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "functionaltitle", nullable = true)
    private String functionaltitle;

    @Column(name = "location", nullable = true)
    private String location;

    @Column(name = "description", nullable = true, columnDefinition = "text")
    private String description;

    @Column(name = "branch", nullable = true)
    private String branch;

    @Column(name = "prevBranch", nullable = true)
    private String prevBranch;

    @Column(name = "isActive", nullable = true)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
