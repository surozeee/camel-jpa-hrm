package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Employee → company admin "Contact Us" message (mobile submit, web inbox).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hrm_employee_contact_message")
public class EmployeeContactMessageEntity extends BaseAuditEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "employee_email", length = 200)
    private String employeeEmail;

    @Column(name = "employee_phone", length = 50)
    private String employeePhone;

    @Column(nullable = false, length = 300)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "read_by")
    private UUID readBy;
}
