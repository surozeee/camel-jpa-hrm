package com.jojolaptech.camel.model.postgres.user.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum GenderEnum {

    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");
    private String gender;
    GenderEnum(String gender) {
        this.gender = gender;
    }
}
