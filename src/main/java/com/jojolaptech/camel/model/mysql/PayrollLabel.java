package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payrollLabel")
@Getter
@Setter
public class PayrollLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "labelName", nullable = false)
    private String labelName;

    @Column(name = "individualName", nullable = false)
    private String individualName;

    @Column(name = "sortingNumber", nullable = false)
    private Integer sortingNumber;

    @Column(name = "beforeTax", nullable = false)
    private Boolean beforeTax;

    @Column(name = "status", nullable = false)
    private Boolean status = true;
}
