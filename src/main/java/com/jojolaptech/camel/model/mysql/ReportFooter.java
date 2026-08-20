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
@Table(name = "reportFooter")
@Getter
@Setter
public class ReportFooter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "leftText", nullable = false)
    private String leftText;

    @Column(name = "rightText", nullable = false)
    private String rightText;

    @Column(name = "isEnabled", nullable = false)
    private boolean isEnabled = true;
}
