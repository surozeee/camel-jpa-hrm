package com.jojolaptech.camel.model.postgres.user;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.user.enums.TrueFalseEnum;
import com.jojolaptech.camel.model.postgres.user.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuEntity extends BaseAuditEntity {

    private String name;
    private String url;
    private String code;
    private String icon;
    private Integer priority;
    @Enumerated(EnumType.STRING)
    private UserTypeEnum userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "has_child_menu")
    @Builder.Default
    private TrueFalseEnum hasChildMenu = TrueFalseEnum.FALSE;

    @ManyToOne
    @JoinColumn(name = "parent_menu_id")
    private MenuEntity parentMenu;

    @OneToMany(mappedBy = "parentMenu", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<MenuEntity> subMenu;

    @OneToMany(mappedBy = "menu", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<MenuPermissionEntity> menuPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Builder.Default
    private List<MenuLocaleEntity> menuLocales = new ArrayList<>();

}
