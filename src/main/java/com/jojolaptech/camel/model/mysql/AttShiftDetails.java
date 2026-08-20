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
@Table(name = "attShiftDetails")
@Getter
@Setter
public class AttShiftDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "days", nullable = false)
    private int days;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isOffDay", nullable = false, length = 1)
    private Boolean isOffDay = false;

    @Column(name = "isHalfDay", nullable = false)
    private Boolean isHalfDay = false;

    @Column(name = "isDisabled", nullable = false)
    private Boolean isDisabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attShift_id", nullable = false)
    private AttShift attShift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attTimeTable_id", nullable = true)
    private AttTimeTable attTimeTable;
}
