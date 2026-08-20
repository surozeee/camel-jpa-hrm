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
@Table(name = "alertSetting")
@Getter
@Setter
public class AlertSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "paramName", nullable = false)
    private String paramName;

    @Column(name = "isEnabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "design", nullable = true, columnDefinition = "text")
    private String design;
}
