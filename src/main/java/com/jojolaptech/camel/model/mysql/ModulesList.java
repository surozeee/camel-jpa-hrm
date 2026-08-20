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
@Table(name = "modulesList")
@Getter
@Setter
public class ModulesList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "moduleName", nullable = false)
    private String moduleName;

    @Column(name = "modulePath", nullable = false)
    private String modulePath;

    @Column(name = "moduleType", nullable = false)
    private String moduleType;
}
