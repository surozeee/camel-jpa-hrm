package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "payrollHeadingPriority")
@Getter
@Setter
public class PayrollHeadingPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "title", nullable = false)
    private String title;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isPercent", nullable = true, length = 1)
    private Boolean isPercent;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "regularPaid", nullable = true, length = 1)
    private Boolean regularPaid;

    @Column(name = "priority", nullable = false)
    private Integer priority;
}
