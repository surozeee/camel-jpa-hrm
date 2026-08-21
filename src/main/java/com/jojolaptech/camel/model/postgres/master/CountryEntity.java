package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.BaseAuditEntity;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import com.jojolaptech.camel.model.postgres.master.CountryIso2AttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "country")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CountryEntity extends BaseAuditEntity {
    @Column(unique = true, length = 500)
    private String name;
    @Column(length = 500)
    private String nationality;
    @Convert(converter = CountryIso2AttributeConverter.class)
    @Column(unique = true, length = 2)
    private CountryEnum iso2;
    @Column(length = 10)
    private String iso3;
    @Column(length = 50)
    private String teleCode;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "base_currency_id")
    private CurrencyEntity baseCurrency;
    /** Country flag image URL only. */
    @Column(name = "flag_url", length = 2048)
    private String flagUrl;
    /** Country image URL only. */
    @Column(length = 2048)
    private String image;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "region_id")
    private RegionEntity region;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "country_currency", joinColumns = @JoinColumn(name = "country_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "currency_id", referencedColumnName = "id"))
    private List<CurrencyEntity> supportingCurrencies;

    @OneToMany(mappedBy = "country", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<StateEntity> state;

    /**
     * Backward-compatible accessor for older compiled mappers expecting getFlag/setFlag.
     * Canonical persistence field is flagUrl.
     */
    @Deprecated
    public String getFlag() {
        return this.flagUrl;
    }

    /**
     * Backward-compatible mutator for older compiled mappers expecting getFlag/setFlag.
     * Canonical persistence field is flagUrl.
     */
    @Deprecated
    public void setFlag(String flag) {
        this.flagUrl = flag;
    }
}
