package com.jojolaptech.camel.migration;

import com.jojolaptech.camel.qa.MigrationRowCountQaService;
import com.jojolaptech.camel.qa.MigrationRowCountResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs migration steps one-by-one via ProducerTemplate when {@code migration.mode} is
 * {@code from} or {@code single}. Default {@code chain} mode keeps the existing master timer.
 */
@Component
@Order(100)
@RequiredArgsConstructor
public class MigrationStepRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationStepRunner.class);

    private final MigrationProperties migrationProperties;
    private final ProducerTemplate producerTemplate;
    private final MigrationRowCountQaService qaService;

    @Override
    public void run(ApplicationArguments args) {
        if (!migrationProperties.useStepRunner()) {
            return;
        }

        List<String> steps = MigrationSteps.resolve(migrationProperties);
        log.info("==========================================");
        log.info("Step-by-step migration starting");
        log.info("Mode: {}", migrationProperties.getMode());
        log.info("Steps to run: {} (of {})", steps.size(), MigrationSteps.ALL.size());
        log.info("Verify after each: {}", migrationProperties.isVerifyAfterEach());
        log.info("First step: {}", steps.getFirst());
        log.info("Last step:  {}", steps.getLast());
        log.info("==========================================");

        int passed = 0;
        int failed = 0;
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            String endpoint = "direct:" + step;
            log.info(">>> [{}/{}] Running {}", i + 1, steps.size(), step);
            long start = System.currentTimeMillis();
            try {
                producerTemplate.sendBody(endpoint, "");
            } catch (Exception ex) {
                failed++;
                log.error("<<< [{}/{}] FAILED {} after {} ms: {}",
                        i + 1, steps.size(), step, System.currentTimeMillis() - start, ex.getMessage(), ex);
                if (migrationProperties.isFailOnVerifyMismatch()) {
                    throw ex;
                }
                continue;
            }
            long elapsed = System.currentTimeMillis() - start;
            log.info("<<< [{}/{}] Completed {} in {} ms", i + 1, steps.size(), step, elapsed);

            if (migrationProperties.isVerifyAfterEach()) {
                List<MigrationRowCountResult> checks = qaService.runChecksForRoute(step);
                if (checks.isEmpty()) {
                    log.info("QA: no row-count check mapped for route {}", step);
                    passed++;
                    continue;
                }
                long stepFails = checks.stream().filter(r -> !r.passed()).count();
                qaService.logStepReport(step, checks);
                if (stepFails > 0) {
                    failed++;
                    log.warn("QA: {} FAIL(s) for {}", stepFails, step);
                    if (migrationProperties.isFailOnVerifyMismatch()) {
                        throw new IllegalStateException(
                                "Migration verify failed for step " + step + " (" + stepFails + " mismatches)");
                    }
                } else {
                    passed++;
                    log.info("QA: PASS for {}", step);
                }
            } else {
                passed++;
            }
        }

        log.info("==========================================");
        log.info("Step-by-step migration finished");
        log.info("OK/skip-verify: {} | failed: {} | total: {}", passed, failed, steps.size());
        log.info("==========================================");
    }
}
