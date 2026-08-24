package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hrm_device_mac")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DeviceMacEntity extends BaseAuditEntity {

    @Column(nullable = false, unique = true)
    private String macAddress;

    @Column(nullable = false)
    private String deviceName;

    @Column(length = 500)
    private String description;

    private String deviceModel;

    private String deviceSerialNumber;

    private String ipAddress;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    /** Denormalized from User-Service branch → company for queries without a local branch row. */
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
