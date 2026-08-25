package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Branch;
import com.jojolaptech.camel.model.mysql.Company;
import com.jojolaptech.camel.model.postgres.company.BranchAddressEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyAddressEntity;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import java.util.UUID;

final class AddressMigrationMapper {

    private AddressMigrationMapper() {}

    static CompanyAddressEntity companyAddress(Company source, UUID companyId) {
        String streetAddress = OrgMigrationMapper.trimToNull(source.getAddress());
        String fax = OrgMigrationMapper.trimToNull(source.getFax());
        if (streetAddress == null && fax == null) {
            return null;
        }
        CompanyAddressEntity address = CompanyAddressEntity.builder()
                .mysqlId(source.getId())
                .companyId(companyId)
                .streetAddress(streetAddress != null ? streetAddress : fax)
                .streetAddress2(streetAddress != null ? fax : null)
                .country(CountryEnum.NP)
                .build();
        address.setStatus(StatusEnum.ACTIVE);
        return address;
    }

    static BranchAddressEntity branchAddress(Branch source) {
        String streetAddress = OrgMigrationMapper.trimToNull(source.getAddress());
        String fax = OrgMigrationMapper.trimToNull(source.getFaxNo());
        if (streetAddress == null && fax == null) {
            return null;
        }
        BranchAddressEntity address = BranchAddressEntity.builder()
                .streetAddress(streetAddress != null ? streetAddress : fax)
                .country(CountryEnum.NP)
                .additionalInfo(buildAdditionalInfo(streetAddress, fax))
                .build();
        address.setStatus(StatusEnum.ACTIVE);
        return address;
    }

    private static String buildAdditionalInfo(String streetAddress, String fax) {
        if (fax == null || fax.equals(streetAddress)) {
            return null;
        }
        return "Fax: " + fax;
    }
}
