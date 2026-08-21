package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hrm_work_location")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkLocationEntity extends BaseAuditEntity {

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(length = 64)
    private String timezone;

    private Double latitude;

    private Double longitude;

    /** Optional punch radius in meters. Null uses the branch attendance-policy max distance. */
    @Column(name = "geofence_radius_meters")
    private Integer geofenceRadiusMeters;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id")
    private UUID branchId;
}
