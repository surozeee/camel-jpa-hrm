package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.LanguageLevel;
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
@Table(name = "employeeLanguage")
@Getter
@Setter
public class EmployeeLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "speaking", nullable = true)
    private LanguageLevel speaking;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading", nullable = true)
    private LanguageLevel reading;

    @Enumerated(EnumType.STRING)
    @Column(name = "writing", nullable = true)
    private LanguageLevel writing;

    @Enumerated(EnumType.STRING)
    @Column(name = "listining", nullable = true)
    private LanguageLevel listining;

    @Column(name = "myFile", nullable = true)
    private String myFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
