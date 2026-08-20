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
@Table(name = "employeeExperience")
@Getter
@Setter
public class EmployeeExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "address", nullable = true)
    private String address;

    @Column(name = "jobResponsibility", nullable = true)
    private String jobResponsibility;

    @Column(name = "reasonForLeaving", nullable = true)
    private String reasonForLeaving;

    @Column(name = "note", nullable = true)
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start", nullable = true)
    private Date start;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end", nullable = true)
    private Date end;

    @Column(name = "position", nullable = true)
    private String position;

    @Column(name = "isVerified", nullable = true)
    private Boolean isVerified;

    @Column(name = "myFile", nullable = true)
    private String myFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobLevel_id", nullable = false)
    private JobLevel jobLevel;
}
