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
@Table(name = "branch")
@Getter
@Setter
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "branchName", nullable = false)
    private String branchName;

    @Column(name = "code", nullable = true)
    private String code;

    @Column(name = "phoneNo", nullable = true)
    private String phoneNo;

    @Column(name = "address", nullable = true)
    private String address;

    @Column(name = "faxNo", nullable = true)
    private String faxNo;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "isBranch", nullable = true)
    private Boolean isBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branchManager_id", nullable = true)
    private Employee branchManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentBranch_id", nullable = false)
    private Branch parentBranch;
}
