package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.SeminarFundedBy;
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
@Table(name = "employeeSeminar")
@Getter
@Setter
public class EmployeeSeminar {

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

    @Column(name = "organizer", nullable = true)
    private String organizer;

    @Column(name = "place", nullable = true)
    private String place;

    @Column(name = "country", nullable = true)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "seminarFundedBy", nullable = true)
    private SeminarFundedBy seminarFundedBy;

    @Column(name = "myFile", nullable = true)
    private String myFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
