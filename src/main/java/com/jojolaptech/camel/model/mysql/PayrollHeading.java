package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.HeadingType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "payrollHeading")
@Getter
@Setter
public class PayrollHeading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "title", nullable = false)
    private String title;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isPercent", nullable = false, length = 1)
    private Boolean isPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "headingType", nullable = false)
    private HeadingType headingType;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "regularPaid", nullable = false, length = 1)
    private Boolean regularPaid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
