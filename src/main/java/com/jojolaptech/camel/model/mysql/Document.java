package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.DocumentType;
import com.jojolaptech.camel.model.mysql.enums.LicenseType;
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
@Table(name = "document")
@Getter
@Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "myfile", nullable = true)
    private String myfile;

    @Column(name = "type", nullable = true)
    private String type;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "nationality", nullable = true)
    private String nationality;

    @Column(name = "documentNumber", nullable = true)
    private String documentNumber;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issueDate", nullable = true)
    private Date issueDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expireDate", nullable = true)
    private Date expireDate;

    @Column(name = "issuePlace", nullable = true)
    private String issuePlace;

    @Column(name = "issueCounrtry", nullable = true)
    private String issueCounrtry;

    @Column(name = "description", nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "licenseType", nullable = true)
    private LicenseType licenseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "documentType", nullable = true)
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
