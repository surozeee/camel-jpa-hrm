package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.LanguageEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "page_locale", uniqueConstraints = {
        @UniqueConstraint(name = "uk_page_locale", columnNames = {"page_id", "language"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageLocaleEntity extends BaseAuditEntity {

    @Column(nullable = false)
    private String title;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "css_content", columnDefinition = "TEXT")
    private String cssContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private LanguageEnum language;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;
}
