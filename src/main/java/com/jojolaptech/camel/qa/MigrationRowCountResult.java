package com.jojolaptech.camel.qa;

public record MigrationRowCountResult(
        String step,
        String label,
        long mysqlCount,
        long postgresCount,
        long delta,
        MigrationComparisonMode mode,
        boolean passed,
        String notes) {}
