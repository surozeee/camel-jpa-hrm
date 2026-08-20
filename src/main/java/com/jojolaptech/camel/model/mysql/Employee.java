package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.EmployeeEthnicity;
import com.jojolaptech.camel.model.mysql.enums.EmployeeMaritialStatus;
import com.jojolaptech.camel.model.mysql.enums.EmployeeReligion;
import com.jojolaptech.camel.model.mysql.enums.EmployeeTitle;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "employee")
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "birthday", nullable = true)
    private Date birthday;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "photo", nullable = true)
    private String photo;

    @Column(name = "permanentAdd", nullable = true)
    private String permanentAdd;

    @Column(name = "temperoryAdd", nullable = true)
    private String temperoryAdd;

    @Column(name = "temporaryAddType", nullable = true)
    private String temporaryAddType;

    @Column(name = "citizenNumber", nullable = true)
    private String citizenNumber;

    @Column(name = "drivingLicenceNo", nullable = true)
    private String drivingLicenceNo;

    @Column(name = "passportNo", nullable = true)
    private String passportNo;

    @Column(name = "jobType", nullable = true)
    private String jobType;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isIncapacitated", nullable = true, length = 1)
    private Boolean isIncapacitated;

    @Column(name = "height", nullable = true)
    private String height;

    @Column(name = "weight", nullable = true)
    private String weight;

    @Column(name = "bloodGroup", nullable = true)
    private String bloodGroup;

    @Column(name = "cfVehicleType", nullable = true)
    private String cfVehicleType;

    @Column(name = "cfTelephoneType", nullable = true)
    private String cfTelephoneType;

    @Column(name = "cfInternetType", nullable = true)
    private String cfInternetType;

    @Column(name = "committeeMembershipType", nullable = true)
    private String committeeMembershipType;

    @Column(name = "isIncapacitatedRemarks", nullable = true)
    private String isIncapacitatedRemarks;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registerDate", nullable = true)
    private Date registerDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "title", nullable = true)
    private EmployeeTitle title;

    @Column(name = "middleName", nullable = true)
    private String middleName;

    @Column(name = "nepaliName", nullable = true)
    private String nepaliName;

    @Column(name = "fatherName", nullable = true)
    private String fatherName;

    @Column(name = "motherName", nullable = true)
    private String motherName;

    @Column(name = "placeOfBirth", nullable = true)
    private String placeOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "maritialStatus", nullable = true)
    private EmployeeMaritialStatus maritialStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "marriageAnniversary", nullable = true)
    private Date marriageAnniversary;

    @Enumerated(EnumType.STRING)
    @Column(name = "religion", nullable = true)
    private EmployeeReligion religion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ethinicity", nullable = true)
    private EmployeeEthnicity ethinicity;

    @Column(name = "motherTongue", nullable = true)
    private String motherTongue;

    @Column(name = "children", nullable = true)
    private Integer children;

    @Column(name = "hobby", nullable = true)
    private String hobby;

    @Column(name = "hireMethod", nullable = true)
    private String hireMethod;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "contractEndDate", nullable = true)
    private Date contractEndDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "hireDate", nullable = true)
    private Date hireDate;

    @Column(name = "appointmentLetterNo", nullable = true)
    private String appointmentLetterNo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "appointmentLetterDate", nullable = true)
    private Date appointmentLetterDate;

    @Column(name = "reference", nullable = true)
    private String reference;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "salaryCalculationDate", nullable = true)
    private Date salaryCalculationDate;

    @Column(name = "level", nullable = true)
    private String level;

    @Column(name = "hiringLocation", nullable = true)
    private String hiringLocation;

    @Column(name = "birthmark", nullable = true)
    private String birthmark;

    @Column(name = "ein", nullable = true)
    private String ein;

    @Column(name = "handicappedFile", nullable = true)
    private String handicappedFile;

    @Column(name = "currentIdNumber", nullable = true)
    private String currentIdNumber;

    @Column(name = "contractIdNumber", nullable = true)
    private String contractIdNumber;

    @Column(name = "cardSerialNumber", nullable = true)
    private String cardSerialNumber;

    @Column(name = "summary", nullable = true, columnDefinition = "text")
    private String summary;
}
