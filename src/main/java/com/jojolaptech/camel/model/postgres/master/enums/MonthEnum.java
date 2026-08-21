package com.jojolaptech.camel.model.postgres.master.enums;

import com.jojolaptech.camel.model.postgres.enums.MonthTypeEnum;
import lombok.Getter;

@Getter
public enum MonthEnum {
    // English Months
    JANUARY("January", MonthTypeEnum.ENGLISH),
    FEBRUARY("February", MonthTypeEnum.ENGLISH),
    MARCH("March", MonthTypeEnum.ENGLISH),
    APRIL("April", MonthTypeEnum.ENGLISH),
    MAY("May", MonthTypeEnum.ENGLISH),
    JUNE("June", MonthTypeEnum.ENGLISH),
    JULY("July", MonthTypeEnum.ENGLISH),
    AUGUST("August", MonthTypeEnum.ENGLISH),
    SEPTEMBER("September", MonthTypeEnum.ENGLISH),
    OCTOBER("October", MonthTypeEnum.ENGLISH),
    NOVEMBER("November", MonthTypeEnum.ENGLISH),
    DECEMBER("December", MonthTypeEnum.ENGLISH),

    // Nepali Months
    BAISHAKH("Baishakh", MonthTypeEnum.NEPALI),
    JESTHA("Jestha", MonthTypeEnum.NEPALI),
    ASHADH("Ashadh", MonthTypeEnum.NEPALI),
    SHRAWAN("Shrawan", MonthTypeEnum.NEPALI),
    BHADRA("Bhadra", MonthTypeEnum.NEPALI),
    ASHWIN("Ashwin", MonthTypeEnum.NEPALI),
    KARTIK("Kartik", MonthTypeEnum.NEPALI),
    MANGSIR("Mangsir", MonthTypeEnum.NEPALI),
    POUSH("Poush", MonthTypeEnum.NEPALI),
    MAGH("Magh", MonthTypeEnum.NEPALI),
    FALGUN("Falgun", MonthTypeEnum.NEPALI),
    CHAITRA("Chaitra", MonthTypeEnum.NEPALI);

    private final String month;
    private final MonthTypeEnum monthType;

    MonthEnum(String month, MonthTypeEnum monthType) {
        this.month = month;
        this.monthType = monthType;
    }
}

