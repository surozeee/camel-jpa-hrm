package com.jojolaptech.camel.model.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.recruitment.enums.CandidateSourceEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
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
@Table(
        name = "hrm_recruitment_candidate",
        indexes = {
            @Index(name = "idx_candidate_company", columnList = "company_id"),
            @Index(name = "idx_candidate_code", columnList = "candidate_code", unique = true)
        })
public class RecruitmentCandidateEntity extends BaseAuditEntity {

    /** Synthetic: CANDIDATE_MYSQL_ID_OFFSET + employee.mysqlId */
    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(name = "candidate_code", nullable = false, length = 64)
    private String candidateCode;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "mobile", length = 50)
    private String mobile;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 30)
    private String gender;

    @Column(name = "current_address", columnDefinition = "TEXT")
    private String currentAddress;

    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 40)
    private CandidateSourceEnum source;

    @Column(name = "converted_employee_id")
    private UUID convertedEmployeeId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
