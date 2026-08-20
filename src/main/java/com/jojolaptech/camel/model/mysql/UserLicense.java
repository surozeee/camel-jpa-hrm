package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PaidBy;
import jakarta.persistence.Column;
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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "userLicense")
@Getter
@Setter
public class UserLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "paidDate", nullable = false)
    private Date paidDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "validTill", nullable = false)
    private Date validTill;

    @Column(name = "remarkValidTill", nullable = false)
    private String remarkValidTill;

    @Column(name = "userCount", nullable = false)
    private Integer userCount;

    @Column(name = "modalId", nullable = false)
    private String modalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "paidBy", nullable = false)
    private PaidBy paidBy;
}
