package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PublicationType;
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
@Table(name = "employeePublication")
@Getter
@Setter
public class EmployeePublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "publicationName", nullable = false)
    private String publicationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "publicationType", nullable = false)
    private PublicationType publicationType;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "country", nullable = false)
    private String country;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publishedDate", nullable = false)
    private Date publishedDate;

    @Column(name = "myFile", nullable = true)
    private String myFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
