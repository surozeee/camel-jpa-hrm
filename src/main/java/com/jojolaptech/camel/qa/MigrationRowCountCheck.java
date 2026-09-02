package com.jojolaptech.camel.qa;

public record MigrationRowCountCheck(
        String step,
        String label,
        String mysqlSql,
        String postgresSql,
        MigrationComparisonMode mode,
        String notes) {}
