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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "happening")
@Getter
@Setter
public class Happening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "text", nullable = true, columnDefinition = "varchar(5000)")
    private String text;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sentDate", nullable = false)
    private Date sentDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiryDate", nullable = false)
    private Date expiryDate;

    @Column(name = "attachment", nullable = true)
    private String attachment;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "happeningDate", nullable = false)
    private Date happeningDate;

    @Column(name = "receiverEmployee", nullable = false)
    private String receiverEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderEmployee_id", nullable = true)
    private Employee senderEmployee;
}
