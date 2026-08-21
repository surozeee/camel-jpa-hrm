package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Employee portal linkage stored separately from generic {@link UserDetailEntity} profile fields.
 */
@Entity
@Table(
        name = "employee_user_detail",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_user_detail_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_employee_user_detail_employee_id", columnNames = "employee_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmployeeUserEntity extends BaseAuditEntity {

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "employee_id", nullable = false, unique = true)
    private UUID employeeId;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "branch_id")
    private UUID branchId;
}
