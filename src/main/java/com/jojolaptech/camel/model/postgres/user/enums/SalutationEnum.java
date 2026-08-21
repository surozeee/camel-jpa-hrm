package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;

@Getter
public enum SalutationEnum {

    MR("Master"),
    MRS("Mistress"),
    MS("Miss"),
    DR("Doctor"),
    ER("Engineer"),
    ;
    private String gender;
    SalutationEnum(String gender) {
        this.gender = gender;
    }
}
