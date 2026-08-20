package com.jojolaptech.camel.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stages")
@Getter
@Setter
public class Stages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "acceptValue", nullable = true)
    private String acceptValue;

    @Column(name = "rejectValue", nullable = true)
    private String rejectValue;

    @Column(name = "holdValue", nullable = true)
    private String holdValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userRating_id", nullable = true)
    private UserRating userRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;
}
