package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_experience")
public class ExperienceEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(nullable = false)
    private String companyName;

    private String designation;

    @Column(nullable = false)
    private LocalDate joinDate;

    private LocalDate leaveDate;

    private Boolean isCurrentJob;

    @Column(length = 1000)
    private String jobDescription;

    @Column(length = 1000)
    private String responsibilities;

    private String location;

    @Column(length = 500)
    private String reasonForLeaving;

    @Column(length = 500)
    private String remarks;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
}
