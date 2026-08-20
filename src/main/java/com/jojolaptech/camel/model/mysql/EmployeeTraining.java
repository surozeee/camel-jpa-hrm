package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.TrainingFundedBy;
import com.jojolaptech.camel.model.mysql.enums.TrainingType;
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
@Table(name = "employeeTraining")
@Getter
@Setter
public class EmployeeTraining {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = true)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = true)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = true)
    private Date endDate;

    @Column(name = "instituteName", nullable = true)
    private String instituteName;

    @Enumerated(EnumType.STRING)
    @Column(name = "trainingType", nullable = true)
    private TrainingType trainingType;

    @Column(name = "place", nullable = true)
    private String place;

    @Column(name = "country", nullable = true)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "trainingFundedBy", nullable = true)
    private TrainingFundedBy trainingFundedBy;

    @Column(name = "myFile", nullable = true)
    private String myFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
