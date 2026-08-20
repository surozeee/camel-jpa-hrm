package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.DocumentType;
import com.jojolaptech.camel.model.mysql.enums.EmployeeBlood;
import com.jojolaptech.camel.model.mysql.enums.EmployeeGender;
import com.jojolaptech.camel.model.mysql.enums.EmployeeRelation;
import com.jojolaptech.camel.model.mysql.enums.EmployeeTitle;
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
@Table(name = "family")
@Getter
@Setter
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "title", nullable = true)
    private EmployeeTitle title;

    @Column(name = "name", nullable = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation", nullable = true)
    private EmployeeRelation relation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dob", nullable = true)
    private Date dob;

    @Column(name = "isDependent", nullable = true)
    private Boolean isDependent;

    @Column(name = "age", nullable = true)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true)
    private EmployeeGender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloodGroup", nullable = true)
    private EmployeeBlood bloodGroup;

    @Column(name = "occupation", nullable = true)
    private String occupation;

    @Column(name = "contactNumber", nullable = true)
    private String contactNumber;

    @Column(name = "nationality", nullable = true)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "documentType", nullable = true)
    private DocumentType documentType;

    @Column(name = "employeeFile", nullable = true)
    private String employeeFile;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "docIssueDate", nullable = true)
    private Date docIssueDate;

    @Column(name = "docIssuePlace", nullable = true)
    private String docIssuePlace;

    @Column(name = "photograph", nullable = true)
    private String photograph;

    @Column(name = "note", nullable = true)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
