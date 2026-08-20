package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "secUser")
@Getter
@Setter
public class SecUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "enabled", nullable = false, length = 1)
    private boolean enabled;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "accountExpired", nullable = false, length = 1)
    private boolean accountExpired;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "accountLocked", nullable = false, length = 1)
    private boolean accountLocked;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "passwordExpired", nullable = false, length = 1)
    private boolean passwordExpired;
}
