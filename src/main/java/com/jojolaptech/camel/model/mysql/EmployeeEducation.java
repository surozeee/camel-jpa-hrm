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
@Table(name = "employeeEducation")
@Getter
@Setter
public class EmployeeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name_of_institute", nullable = false)
    private String name_of_institute;

    @Column(name = "board", nullable = false)
    private String board;

    @Column(name = "division", nullable = true)
    private String division;

    @Column(name = "level", nullable = false)
    private String level;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dates", nullable = true)
    private Date dates;

    @Column(name = "courseName", nullable = true)
    private String courseName;

    @Column(name = "faculty", nullable = true)
    private String faculty;

    @Column(name = "country", nullable = true)
    private String country;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "passedYear", nullable = true)
    private Date passedYear;

    @Column(name = "isRunning", nullable = true)
    private Boolean isRunning;

    @Column(name = "majorSubject", nullable = true)
    private String majorSubject;

    @Column(name = "gradePercentage", nullable = true)
    private String gradePercentage;

    @Column(name = "myFile", nullable = true)
    private String myFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
