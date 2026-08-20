package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.BankTypeEnum;
import com.jojolaptech.camel.model.mysql.enums.StatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank")
@Getter
@Setter
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "logo", nullable = false)
    private String logo;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BankTypeEnum type;

    @Column(name = "code", nullable = true)
    private String code;

    @Column(name = "shortCode", nullable = true)
    private String shortCode;

    @Column(name = "headOfficeCode", nullable = true)
    private Integer headOfficeCode;

    @Column(name = "address", nullable = true)
    private String address;

    @Column(name = "remarks", nullable = true)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status;
}
