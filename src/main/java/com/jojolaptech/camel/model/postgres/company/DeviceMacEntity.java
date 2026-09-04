package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hrm_device_mac")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DeviceMacEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

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

    /** Denormalized from branch → company for queries without a local branch join. */
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
