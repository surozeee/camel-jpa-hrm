package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.ApplyFor;
import com.jojolaptech.camel.model.mysql.enums.LeaveCategory;
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
@Table(name = "leaves")
@Getter
@Setter
public class Leaves {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "applyFor", nullable = false)
    private ApplyFor applyFor;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "isActive", nullable = false, length = 1)
    private Boolean isActive;

    @Column(name = "isOfficialTrip", nullable = true)
    private Boolean isOfficialTrip;

    @Enumerated(EnumType.STRING)
    @Column(name = "leaveCategory", nullable = true)
    private LeaveCategory leaveCategory;

    @Column(name = "leaveName", nullable = false)
    private String leaveName;

    @Column(name = "applyForMaleFemale", nullable = true)
    private String applyForMaleFemale;

    @Column(name = "maxDay", nullable = false)
    private double maxDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
