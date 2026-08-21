package com.jojolaptech.camel.model.postgres.company.enums;

/**
 * What to do with leave types not listed in the FY closing carry-forward whitelist.
 */
public enum FyClosingNonSelectedLeaveActionEnum {
    /** Do not change balances for non-selected leave types (legacy default). */
    SKIP,
    /** Zero remaining balance when company carry-forward transfer runs. */
    LAPSE
}
