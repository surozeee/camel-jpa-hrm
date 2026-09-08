package com.jojolaptech.camel.migration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    /**
     * chain = existing master timer (all steps).
     * from = run from {@link #fromStep} through the end via step runner.
     * single = run only {@link #onlyStep} via step runner.
     */
    private String mode = "chain";

    /** When mode=from, first route id to run (inclusive), e.g. attendance-transaction-migration. */
    private String fromStep = "";

    /** When mode=single, the only route id to run. */
    private String onlyStep = "";

    /** After each step in from/single mode, run MySQL↔PG row-count checks for that step. */
    private boolean verifyAfterEach = true;

    /** When true and a verify check fails, abort remaining steps. */
    private boolean failOnVerifyMismatch = false;

    public boolean useStepRunner() {
        return "from".equalsIgnoreCase(mode) || "single".equalsIgnoreCase(mode);
    }

    public boolean useMasterChain() {
        return !useStepRunner();
    }
}
