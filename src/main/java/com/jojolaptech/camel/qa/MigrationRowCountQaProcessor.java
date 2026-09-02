package com.jojolaptech.camel.qa;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MigrationRowCountQaProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MigrationRowCountQaProcessor.class);

    private final MigrationRowCountQaService qaService;
    private final MigrationQaProperties qaProperties;

    @Override
    public void process(Exchange exchange) {
        if (!qaProperties.isEnabled()) {
            log.info("Migration row-count QA is disabled (migration.qa.enabled=false)");
            exchange.setProperty("migrationQaPassed", true);
            return;
        }

        List<MigrationRowCountResult> sourceChecks = qaService.runSourceChecks();
        List<MigrationRowCountResult> pipelineChecks = qaService.runPipelineChecks(exchange);
        qaService.logReport(sourceChecks, pipelineChecks);

        long failures = sourceChecks.stream().filter(result -> !result.passed()).count()
                + pipelineChecks.stream().filter(result -> !result.passed()).count();
        boolean passed = failures == 0;
        exchange.setProperty("migrationQaPassed", passed);
        exchange.setProperty("migrationQaFailureCount", failures);

        if (!passed && qaProperties.isFailOnMismatch()) {
            throw new IllegalStateException("Migration row-count QA failed with " + failures + " mismatches");
        }
    }
}
