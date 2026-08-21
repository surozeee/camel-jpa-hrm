package com.jojolaptech.camel.route;

import com.jojolaptech.camel.processor.PrivilegeProcessor;
import com.jojolaptech.camel.processor.RoleProcessor;
import com.jojolaptech.camel.processor.UserProcessor;
import com.jojolaptech.camel.repository.mysql.RequestmapRepository;
import com.jojolaptech.camel.repository.mysql.SecRoleRepository;
import com.jojolaptech.camel.repository.mysql.SecUserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImportRouteBuilder extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(ImportRouteBuilder.class);

    private static final int PAGE_SIZE = 100;
    private static final int MIGRATION_THROTTLE_MS = 1000;

    private final PrivilegeProcessor privilegeProcessor;
    private final RoleProcessor roleProcessor;
    private final UserProcessor userProcessor;
    private final RequestmapRepository requestmapRepository;
    private final SecRoleRepository secRoleRepository;
    private final SecUserRepository secUserRepository;

    @Override
    public void configure() {
        errorHandler(defaultErrorHandler()
                .maximumRedeliveries(3)
                .redeliveryDelay(2000));

        from("timer:master-import?repeatCount=1&delay=0")
                .routeId("master-migration-route")
                .process(exchange -> {
                    long startTime = System.currentTimeMillis();
                    exchange.setProperty("startTime", startTime);
                    LocalDateTime startDateTime = LocalDateTime.now();
                    exchange.setProperty("startDateTime", startDateTime);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                    log.info("==========================================");
                    log.info("Starting HRM user/role/privilege migration...");
                    log.info("Start Time: {}", startDateTime.format(formatter));
                    log.info("Page Size: {}", PAGE_SIZE);
                    log.info("==========================================");
                })
                .to("direct:privilege-migration")
                .log("Step 1 completed: privilege-migration")
                .process(exchange -> {
                    System.gc();
                    Thread.sleep(MIGRATION_THROTTLE_MS);
                })
                .to("direct:role-migration")
                .log("Step 2 completed: role-migration")
                .process(exchange -> {
                    System.gc();
                    Thread.sleep(MIGRATION_THROTTLE_MS);
                })
                .to("direct:user-migration")
                .log("Step 3 completed: user-migration")
                .process(exchange -> {
                    long endTime = System.currentTimeMillis();
                    long startTime = exchange.getProperty("startTime", Long.class);
                    long totalTime = endTime - startTime;
                    LocalDateTime endDateTime = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

                    long hours = totalTime / (1000 * 60 * 60);
                    long minutes = (totalTime % (1000 * 60 * 60)) / (1000 * 60);
                    long seconds = (totalTime % (1000 * 60)) / 1000;
                    long milliseconds = totalTime % 1000;

                    int privilegeCount = exchange.getProperty("privilegeCount", 0, Integer.class);
                    int roleCount = exchange.getProperty("roleCount", 0, Integer.class);
                    int userCount = exchange.getProperty("userCount", 0, Integer.class);

                    log.info("==========================================");
                    log.info("User/role/privilege migration completed!");
                    log.info("Start Time: {}", exchange.getProperty("startDateTime", LocalDateTime.class).format(formatter));
                    log.info("End Time: {}", endDateTime.format(formatter));
                    log.info("Total Time: {} hours, {} minutes, {} seconds, {} milliseconds",
                            hours, minutes, seconds, milliseconds);
                    log.info("--------------------------------------------");
                    log.info("1. privilege (requestmap -> permission): {}", privilegeCount);
                    log.info("2. role (secRole -> role):               {}", roleCount);
                    log.info("3. user (secUser -> users):              {}", userCount);
                    log.info("--------------------------------------------");
                    log.info("GRAND TOTAL:                             {}", privilegeCount + roleCount + userCount);
                    log.info("==========================================");
                });

        from("direct:privilege-migration")
                .routeId("privilege-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = requestmapRepository.findAll(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                        log.info("Fetched requestmap page={}, size={}, returnedRows={}, hasNext={}",
                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No requestmap rows in this page, continuing...")
                        .otherwise()
                            .process(privilegeProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "privilege-migration", "privilegeCount"));

        from("direct:role-migration")
                .routeId("role-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = secRoleRepository.findAll(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                        log.info("Fetched secRole page={}, size={}, returnedRows={}, hasNext={}",
                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No secRole rows in this page, continuing...")
                        .otherwise()
                            .process(roleProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "role-migration", "roleCount"));

        from("direct:user-migration")
                .routeId("user-migration")
                .setProperty("page").constant(0)
                .setProperty("hasNext").constant(true)
                .setProperty("importCount").constant(0)
                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))
                    .process(exchange -> {
                        int page = exchange.getProperty("page", Integer.class);
                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
                        var resultPage = secUserRepository.findAll(pageable);
                        exchange.getMessage().setBody(resultPage.getContent());
                        exchange.setProperty("hasNext", resultPage.hasNext());
                        exchange.setProperty("page", page + 1);
                        log.info("Fetched secUser page={}, size={}, returnedRows={}, hasNext={}",
                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());
                    })
                    .choice()
                        .when(simple("${body.size} == 0"))
                            .log("No secUser rows in this page, continuing...")
                        .otherwise()
                            .process(userProcessor)
                            .process(exchange -> addImported(exchange))
                    .end()
                .end()
                .process(exchange -> finishCount(exchange, "user-migration", "userCount"));
    }

    private static void addImported(org.apache.camel.Exchange exchange) {
        Integer imported = exchange.getProperty("batchImported", 0, Integer.class);
        Integer count = exchange.getProperty("importCount", Integer.class);
        exchange.setProperty("importCount", count + imported);
    }

    private static void finishCount(org.apache.camel.Exchange exchange, String routeName, String countProperty) {
        Integer totalCount = exchange.getProperty("importCount", Integer.class);
        log.info("==========================================");
        log.info("{} completed!", routeName);
        log.info("Total records imported: {}", totalCount);
        log.info("==========================================");
        exchange.setProperty(countProperty, totalCount);
    }
}
