package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "templatePayrollHeading")
@Getter
@Setter
public class TemplatePayrollHeading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payrollTemplate_id", nullable = false)
    private PayrollHeadingTemplate payrollTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payrollHeading_id", nullable = false)
    private PayrollSystemHeading payrollHeading;
}
