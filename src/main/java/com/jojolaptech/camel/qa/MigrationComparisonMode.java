package com.jojolaptech.camel.qa;

public enum MigrationComparisonMode {
    /** PG count must equal MySQL count. */
    EQUAL,
    /** PG may be lower when rows are skipped (missing FK, invalid data). */
    PG_AT_MOST_MYSQL,
    /** PG may be higher when one source row fans out to many targets. */
    PG_AT_LEAST_MYSQL,
    /** Log counts only; no pass/fail. */
    INFO
}
