package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PaymentStats;
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
import java.math.BigInteger;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscriptionPayment")
@Getter
@Setter
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "payAmount", nullable = false)
    private Double payAmount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payDate", nullable = false)
    private Date payDate;

    @Column(name = "subscriptionType", nullable = false)
    private Integer subscriptionType;

    @Column(name = "paymentType", nullable = false)
    private String paymentType;

    @Column(name = "billNumber", nullable = true)
    private BigInteger billNumber;

    @Column(name = "bankName", nullable = true)
    private String bankName;

    @Column(name = "voucherNumber", nullable = true)
    private BigInteger voucherNumber;

    @Column(name = "scanImage", nullable = true)
    private String scanImage;

    @Column(name = "paymentOf", nullable = false)
    private String paymentOf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStats", nullable = false)
    private PaymentStats paymentStats;

    @Column(name = "paidBy", nullable = false)
    private String paidBy;
}
