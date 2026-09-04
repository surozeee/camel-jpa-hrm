package com.jojolaptech.camel.model.postgres.recruitment;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.recruitment.enums.PublicationChannelEnum;
import com.jojolaptech.camel.model.postgres.recruitment.enums.PublicationPostingStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
        name = "hrm_recruitment_vacancy_publication",
        indexes = {
            @Index(name = "idx_vpub_vacancy", columnList = "vacancy_id"),
            @Index(name = "idx_vpub_company", columnList = "company_id")
        })
public class RecruitmentVacancyPublicationEntity extends BaseAuditEntity {

    @Column(name = "mysql_id", unique = true)
    private Long mysqlId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private RecruitmentVacancyEntity vacancy;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 32)
    private PublicationChannelEnum channel;

    @Column(name = "outlet_name", nullable = false, length = 255)
    private String outletName;

    @Column(name = "document_url", length = 1000)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "posting_status", nullable = false, length = 24)
    @Builder.Default
    private PublicationPostingStatusEnum postingStatus = PublicationPostingStatusEnum.LIVE;

    @Column(name = "remarks", length = 1000)
    private String remarks;
}
