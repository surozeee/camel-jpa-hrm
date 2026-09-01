package com.jojolaptech.camel.model.postgres.company.enums;

import lombok.Getter;

@Getter
public enum RelationshipEnum {
    SPOUSE("Spouse"),
    CHILD("Child"),
    PARENT("Parent"),
    SIBLING("Sibling"),
    FATHER("Father"),
    MOTHER("Mother"),
    SON("Son"),
    DAUGHTER("Daughter"),
    BROTHER("Brother"),
    SISTER("Sister"),
    OTHER("Other");

    private final String displayName;

    RelationshipEnum(String displayName) {
        this.displayName = displayName;
    }
}
