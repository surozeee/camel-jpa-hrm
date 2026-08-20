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
@Table(name = "payPlan")
@Getter
@Setter
public class PayPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "startNo", nullable = false)
    private Integer startNo;

    @Column(name = "endNo", nullable = false)
    private Integer endNo;

    @Column(name = "noOfDays", nullable = false)
    private Integer noOfDays;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "discountPercent", nullable = false)
    private Double discountPercent;

    @Column(name = "discountAmount", nullable = false)
    private Double discountAmount;

    @Column(name = "subscriptionDisPercent", nullable = false)
    private Double subscriptionDisPercent;

    @Column(name = "subscriptionDisAmount", nullable = false)
    private Double subscriptionDisAmount;

    @Column(name = "netAmount", nullable = false)
    private Double netAmount;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payType_id", nullable = false)
    private PayType payType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costType_id", nullable = false)
    private CostType costType;
}
