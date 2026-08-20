package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "attHolidayList")
@Getter
@Setter
public class AttHolidayList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "holidayName", nullable = false)
    private String holidayName;

    @Column(name = "applicableFor", nullable = false)
    private String applicableFor;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isActive", nullable = true, length = 1)
    private Boolean isActive;

    @Column(name = "applyForMaleFemale", nullable = true)
    private String applyForMaleFemale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
