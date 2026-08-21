package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.LanguageEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_locale")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuLocaleEntity extends BaseAuditEntity {

    private String name;
    @Enumerated(EnumType.STRING)
    private LanguageEnum language;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;
}
