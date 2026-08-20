package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.AddressType;
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
@Table(name = "employeeAddress")
@Getter
@Setter
public class EmployeeAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "state", nullable = true)
    private String state = "";

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "zipcode", nullable = false)
    private String zipcode;

    @Column(name = "nation", nullable = false)
    private String nation;

    @Column(name = "zone", nullable = true)
    private String zone;

    @Column(name = "district", nullable = true)
    private String district;

    @Column(name = "vdcMunicipality", nullable = true)
    private String vdcMunicipality;

    @Column(name = "wardNo", nullable = true)
    private String wardNo;

    @Column(name = "houseNo", nullable = true)
    private String houseNo;

    @Column(name = "locality", nullable = true)
    private String locality;

    @Enumerated(EnumType.STRING)
    @Column(name = "addressType", nullable = true)
    private AddressType addressType;

    @Column(name = "address", nullable = false)
    private String address = street;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
