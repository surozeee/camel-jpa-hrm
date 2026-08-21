package com.jojolaptech.camel.model.postgres.master;

import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts CountryEnum to/from String (ISO2 code) for the database column (varchar).
 * Uses enum name (e.g. "NP", "IN") to avoid PostgreSQL smallint cast issues and support existing data.
 */
@Converter(autoApply = false)
public class CountryIso2AttributeConverter implements AttributeConverter<CountryEnum, String> {

    @Override
    public String convertToDatabaseColumn(CountryEnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public CountryEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return CountryEnum.fromCode(dbData.trim());
    }
}
