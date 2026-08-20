package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PaymentStatus;
import com.jojolaptech.camel.model.mysql.enums.ThirdPartyPaymentType;
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
@Table(name = "companyValidity")
@Getter
@Setter
public class CompanyValidity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "validFrom", nullable = false)
    private Date validFrom;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "validTill", nullable = false)
    private Date validTill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "totalEmployee", nullable = false)
    private Double totalEmployee;

    @Column(name = "payAmount", nullable = false)
    private Double payAmount;

    @Column(name = "manualDiscountAmount", nullable = true)
    private Double manualDiscountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStatus", nullable = false)
    private PaymentStatus paymentStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payDate", nullable = false)
    private Date payDate;

    @Column(name = "transactionId", nullable = false)
    private String transactionId;

    @Column(name = "paidSubscriptionType", nullable = true)
    private String paidSubscriptionType;

    @Column(name = "paidEditionType", nullable = false)
    private String paidEditionType;

    @Column(name = "voucherPath", nullable = true)
    private String voucherPath;

    @Column(name = "voucherNo", nullable = true)
    private String voucherNo;

    @Column(name = "modules", nullable = false)
    private String modules = "1";

    @Enumerated(EnumType.STRING)
    @Column(name = "thirdPartyPaymentType", nullable = true)
    private ThirdPartyPaymentType thirdPartyPaymentType;
}
