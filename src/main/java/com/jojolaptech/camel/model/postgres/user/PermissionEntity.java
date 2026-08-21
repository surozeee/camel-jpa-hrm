package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.enums.PermissionForEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permission")
public class PermissionEntity extends BaseAuditEntity {

    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "permission_group_id")
    private PermissionGroupEntity permissionGroup;

    /** Scopes where this permission applies (stored as JSON array, e.g. ["SYSTEM","ORGANIZATION"]). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permission_for")
    @Builder.Default
    private List<PermissionForEnum> permissionFor = new ArrayList<>();

}
