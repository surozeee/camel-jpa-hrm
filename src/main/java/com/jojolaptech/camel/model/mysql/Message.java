package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.MessageCategory;
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
@Table(name = "message")
@Getter
@Setter
public class Message {

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

    @Column(name = "senderEmployee", nullable = true)
    private String senderEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "messageCategory", nullable = true)
    private MessageCategory messageCategory = MessageCategory.GENERAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiverEmployee_id", nullable = false)
    private Employee receiverEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
