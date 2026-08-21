package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.PreEmploymentCollectionKind;
import com.jojolaptech.camel.model.postgres.company.enums.PreEmploymentRequirementCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hrm_pre_employment_requirement")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreEmploymentRequirementEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PreEmploymentRequirementCategory category;

    @Column(nullable = false)
    private Boolean mandatory;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_kind", length = 40)
    private PreEmploymentCollectionKind collectionKind;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
