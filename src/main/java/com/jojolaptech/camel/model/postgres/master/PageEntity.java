package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "page", uniqueConstraints = {
        @UniqueConstraint(name = "uk_page_name", columnNames = "name")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageEntity extends BaseAuditEntity {

    /** URL slug for Next.js route /page/{name}, e.g. about-us */
    @Column(nullable = false, length = 128, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "css_content", columnDefinition = "TEXT")
    private String cssContent;

    @Builder.Default
    private Integer priority = 0;

    @OneToMany(mappedBy = "page", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = false)
    @Builder.Default
    private List<PageLocaleEntity> pageLocales = new ArrayList<>();
}
