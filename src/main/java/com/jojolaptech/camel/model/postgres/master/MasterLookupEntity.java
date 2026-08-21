package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.master.enums.MasterLookupCategoryEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "master_lookup",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_master_lookup_category_code",
                columnNames = {"category", "code"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterLookupEntity extends BaseAuditEntity {

    /**
     * Stored as VARCHAR (not a Postgres CHECK enum). Avoids startup failures when
     * {@link MasterLookupCategoryEnum} grows — Hibernate CHECK constraints are not auto-updated.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 64)
    private MasterLookupCategoryEnum category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 64)
    private String code;

    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
