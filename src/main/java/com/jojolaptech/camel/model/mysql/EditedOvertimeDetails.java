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
@Table(name = "editedOvertimeDetails")
@Getter
@Setter
public class EditedOvertimeDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendanceTransaction_id", nullable = false)
    private AttendanceTransaction attendanceTransaction;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "editedDate", nullable = false)
    private Date editedDate;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @Column(name = "previousOverTime", nullable = false)
    private String previousOverTime;
}
