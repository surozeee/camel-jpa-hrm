package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pricingEstimateEmailDetails")
@Getter
@Setter
public class PricingEstimateEmailDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "firstName", nullable = true)
    private String firstName;

    @Column(name = "lastName", nullable = true)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "companyUrl", nullable = true)
    private String companyUrl;

    @Column(name = "phone", nullable = true)
    private BigInteger phone;

    @Column(name = "fax", nullable = true)
    private BigInteger fax;

    @Column(name = "title", nullable = true)
    private String title;

    @Column(name = "company", nullable = true)
    private String company;

    @Column(name = "estimateParameter", nullable = true)
    private String estimateParameter;
}
