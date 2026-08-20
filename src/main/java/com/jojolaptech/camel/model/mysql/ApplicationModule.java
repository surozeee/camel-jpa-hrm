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
@Table(name = "applicationModule")
@Getter
@Setter
public class ApplicationModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "moduleName", nullable = false)
    private String moduleName;

    @Column(name = "freeDays", nullable = false)
    private Integer freeDays;

    @Column(name = "freeUsers", nullable = false)
    private Integer freeUsers;

    @Column(name = "discount", nullable = false)
    private Double discount;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @Column(name = "mainModule", nullable = false)
    private Boolean mainModule = false;
}
