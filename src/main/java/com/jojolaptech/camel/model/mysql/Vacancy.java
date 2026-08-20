package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "vacancy")
@Getter
@Setter
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "jobTitle", nullable = false)
    private String jobTitle;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = false)
    private Date endDate;

    @Column(name = "jobType", nullable = false)
    private String jobType;

    @Column(name = "hireType", nullable = false)
    private String hireType;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "budgeted", nullable = false, length = 1)
    private Boolean budgeted;

    @Column(name = "numberOfOpenings", nullable = true)
    private Integer numberOfOpenings;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "targetedJobStartDate", nullable = true)
    private Date targetedJobStartDate;

    @Column(name = "reasonForVacancy", nullable = true)
    private String reasonForVacancy;

    @Column(name = "salaryType", nullable = false)
    private String salaryType;

    @Column(name = "salaryCurrency", nullable = true)
    private String salaryCurrency;

    @Column(name = "salaryPaymentType", nullable = true)
    private String salaryPaymentType;

    @Column(name = "minSalary", nullable = true)
    private Double minSalary;

    @Column(name = "maxSalary", nullable = true)
    private Double maxSalary;

    @Column(name = "minAge", nullable = true)
    private Integer minAge;

    @Column(name = "maxAge", nullable = true)
    private Integer maxAge;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "preferredQualification", nullable = true)
    private String preferredQualification;

    @Column(name = "educationDescription", nullable = true)
    private String educationDescription;

    @Column(name = "minExperience", nullable = true)
    private Integer minExperience;

    @Column(name = "maxExperience", nullable = true)
    private Integer maxExperience;

    @Column(name = "jobSpecification", nullable = false)
    private String jobSpecification;

    @Column(name = "otherSpecification", nullable = true)
    private String otherSpecification;

    @Column(name = "jobLocation", nullable = false)
    private String jobLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiringManager_id", nullable = true)
    private Employee hiringManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobCategory_id", nullable = false)
    private JobCategory jobCategory;
}
