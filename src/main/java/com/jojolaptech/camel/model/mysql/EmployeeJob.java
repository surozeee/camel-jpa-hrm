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
@Table(name = "employeeJob")
@Getter
@Setter
public class EmployeeJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobTitle job;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startdate", nullable = true)
    private Date startdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "enddate", nullable = true)
    private Date enddate;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isactive", nullable = true, length = 1)
    private Boolean isactive;
}
