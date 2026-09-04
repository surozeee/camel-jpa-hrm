package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "hrm_grade")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradeEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Lower number = more senior. Used for ordering and band comparison. */
    @Column(name = "sort_rank")
    private Integer sortRank;

    @Column(name = "min_salary", precision = 19, scale = 4)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 19, scale = 4)
    private BigDecimal maxSalary;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
