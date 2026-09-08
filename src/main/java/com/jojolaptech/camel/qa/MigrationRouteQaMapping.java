package com.jojolaptech.camel.qa;

import java.util.List;
import java.util.Map;

/** Maps Camel route ids → MigrationRowCountCheck.step ids used in QA SQL. */
public final class MigrationRouteQaMapping {

    private static final Map<String, List<String>> ROUTE_TO_STEPS = Map.ofEntries(
            Map.entry("privilege-migration", List.of("1")),
            Map.entry("role-migration", List.of("2")),
            Map.entry("role-permission-migration", List.of("2a")),
            Map.entry("user-migration", List.of("24")),
            Map.entry("company-migration", List.of("3")),
            Map.entry("company-address-migration", List.of("4")),
            Map.entry("branch-migration", List.of("5")),
            Map.entry("branch-address-migration", List.of("6")),
            Map.entry("taxation-migration", List.of("8")),
            Map.entry("attendance-log-migration", List.of("23h")),
            Map.entry("attendance-transaction-migration", List.of("23i")),
            Map.entry("attendance-forgot-migration", List.of("23j")),
            Map.entry("device-logs-migration", List.of("23r")),
            Map.entry("temp-device-logs-migration", List.of("23s")),
            Map.entry("old-attendance-transaction-migration", List.of("23t")),
            Map.entry("work-shift-migration", List.of("23u")),
            Map.entry("edited-overtime-details-migration", List.of("23v")),
            Map.entry("emp-temp-shift-migration", List.of("22h")),
            Map.entry("company-employee-params-migration", List.of("18d")),
            Map.entry("employee-summary-migration", List.of("18e")));

    private MigrationRouteQaMapping() {}

    public static List<String> checkStepsForRoute(String routeId) {
        if (routeId == null || routeId.isBlank()) {
            return List.of();
        }
        return ROUTE_TO_STEPS.getOrDefault(routeId.trim(), List.of());
    }
}
