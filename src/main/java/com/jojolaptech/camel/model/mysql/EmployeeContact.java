package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.ContactType;
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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employeeContact")
@Getter
@Setter
public class EmployeeContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "extension", nullable = true)
    private String extension;

    @Column(name = "mobile", nullable = true)
    private String mobile;

    @Column(name = "emergencyContactName", nullable = false)
    private String emergencyContactName;

    @Column(name = "emergencyContactRelation", nullable = true)
    private String emergencyContactRelation;

    @Column(name = "emergencyContactPhone", nullable = true)
    private String emergencyContactPhone;

    @Column(name = "emergencyContactMobile", nullable = true)
    private String emergencyContactMobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "contactType", nullable = true)
    private ContactType contactType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
