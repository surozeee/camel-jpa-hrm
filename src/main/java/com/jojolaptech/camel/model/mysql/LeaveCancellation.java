package com.jojolaptech.camel.model.mysql;

import com.jojolaptech.camel.model.mysql.enums.RequestStatus;
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
@Table(name = "leaveCancellation")
@Getter
@Setter
public class LeaveCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "requestDate", nullable = true)
    private Date requestDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "responseDate", nullable = true)
    private Date responseDate;

    @Column(name = "reason", nullable = true)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondByEmp_id", nullable = true)
    private Employee respondByEmp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondByCompany_id", nullable = true)
    private Company respondByCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leaveApplication_id", nullable = false)
    private LeaveApplication leaveApplication;
}
