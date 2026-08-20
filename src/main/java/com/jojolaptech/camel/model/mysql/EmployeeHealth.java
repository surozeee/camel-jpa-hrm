package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.EmployeeHealthStatus;
import com.jojolaptech.camel.model.mysql.enums.HealthCondition;
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
@Table(name = "employeeHealth")
@Getter
@Setter
public class EmployeeHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "healthStatus", nullable = true)
    private EmployeeHealthStatus healthStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "healthCondition", nullable = true)
    private HealthCondition healthCondition;

    @Column(name = "diagnosed", nullable = true)
    private Boolean diagnosed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "diagonosedDate", nullable = true)
    private Date diagonosedDate;

    @Column(name = "hospital", nullable = true)
    private String hospital;

    @Column(name = "doctorName", nullable = true)
    private String doctorName;

    @Column(name = "onGoingTreatment", nullable = true)
    private Boolean onGoingTreatment;

    @Column(name = "hospitalAddress", nullable = true)
    private String hospitalAddress;

    @Column(name = "doctorNumber", nullable = true)
    private String doctorNumber;

    @Column(name = "detail", nullable = true)
    private String detail;

    @Column(name = "healthFile", nullable = true)
    private String healthFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
