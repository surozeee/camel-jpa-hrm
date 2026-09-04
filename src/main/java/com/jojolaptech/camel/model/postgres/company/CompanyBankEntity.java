package com.jojolaptech.camel.model.postgres.company;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.company.enums.AccountTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hrm_company_bank")
public class CompanyBankEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @Column(nullable = false)
    private UUID bankId;

    @Column(nullable = false)
    private String accountNumber;

    private String bankBranch;

    private String accountHolderName;

    @Enumerated(EnumType.STRING)
    private AccountTypeEnum accountType;

    private Boolean isPrimary;

    @Column(length = 500)
    private String remarks;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}
