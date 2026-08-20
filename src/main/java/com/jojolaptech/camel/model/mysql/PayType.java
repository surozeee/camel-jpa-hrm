package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payType")
@Getter
@Setter
public class PayType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "subscriptionBasis", nullable = false, unique = true)
    private String subscriptionBasis;

    @Column(name = "discount", nullable = false)
    private Double discount;

    @Column(name = "dayCount", nullable = false)
    private Double dayCount;
}
