package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "companyEmployee")
@Getter
@Setter
public class CompanyEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "joinDate", nullable = false)
    private Date joinDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "terminationDate", nullable = true)
    private Date terminationDate;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isActive", nullable = false, length = 1)
    private boolean isActive;

    @Column(name = "enrollId", nullable = true)
    private Integer enrollId;

    @Column(name = "organizationId", nullable = false)
    private String organizationId;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "taxPaidByCompany", nullable = false, length = 1)
    private boolean taxPaidByCompany = false;
}
