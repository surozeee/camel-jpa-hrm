package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.ModuleStatus;
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
@Table(name = "company")
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "url", nullable = true)
    private String url;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "address", nullable = true)
    private String address;

    @Column(name = "fax", nullable = true)
    private String fax;

    @Column(name = "logo", nullable = true)
    private String logo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registerDate", nullable = true)
    private Date registerDate;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isArchive", nullable = false, length = 1)
    private Boolean isArchive = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "pimsModule", nullable = true)
    private ModuleStatus pimsModule = ModuleStatus.Disabled;

    @Column(name = "branchDepartmentEnabled", nullable = false)
    private Boolean branchDepartmentEnabled = false;

    @Column(name = "remoteLoginEnabled", nullable = false)
    private Boolean remoteLoginEnabled = false;

    @Column(name = "verifyLeaveAccumulation", nullable = false)
    private Boolean verifyLeaveAccumulation = false;

    @Column(name = "payrollPaymentConfigEnabled", nullable = false)
    private Boolean payrollPaymentConfigEnabled = false;
}
