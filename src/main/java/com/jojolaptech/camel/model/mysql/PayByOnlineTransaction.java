package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.PaymentResponseStatus;
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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payByOnlineTransaction")
@Getter
@Setter
public class PayByOnlineTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyValidityId_id", nullable = false)
    private CompanyValidity companyValidityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentResponseStatus", nullable = false)
    private PaymentResponseStatus paymentResponseStatus;

    @Column(name = "transactionId", nullable = true)
    private String transactionId;
}
