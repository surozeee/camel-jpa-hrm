package com.jojolaptech.camel.route;



import com.jojolaptech.camel.processor.AttParamsProcessor;

import com.jojolaptech.camel.processor.BranchAddressProcessor;

import com.jojolaptech.camel.processor.BranchLeaveTypeProcessor;

import com.jojolaptech.camel.processor.BranchProcessor;

import com.jojolaptech.camel.processor.CompanyAddressProcessor;

import com.jojolaptech.camel.processor.CompanyProcessor;

import com.jojolaptech.camel.processor.DepartmentParentLinkProcessor;

import com.jojolaptech.camel.processor.DepartmentProcessor;

import com.jojolaptech.camel.processor.FiscalYearClosingParameterProcessor;

import com.jojolaptech.camel.processor.FiscalYearProcessor;

import com.jojolaptech.camel.processor.LeaveTypeProcessor;

import com.jojolaptech.camel.processor.PayrollRuleProcessor;

import com.jojolaptech.camel.processor.PrivilegeProcessor;

import com.jojolaptech.camel.processor.RoleProcessor;

import com.jojolaptech.camel.processor.TaxationProcessor;

import com.jojolaptech.camel.processor.UserDetailProcessor;

import com.jojolaptech.camel.processor.UserProcessor;

import com.jojolaptech.camel.repository.mysql.BranchDepartmentRepository;

import com.jojolaptech.camel.repository.mysql.BranchRepository;

import com.jojolaptech.camel.repository.mysql.CompanyFiscalYearClosingParameterRepository;

import com.jojolaptech.camel.repository.mysql.CompanyRepository;

import com.jojolaptech.camel.repository.mysql.DepartmentRepository;

import com.jojolaptech.camel.repository.mysql.FiscalYearRepository;

import com.jojolaptech.camel.repository.mysql.LeavesRepository;

import com.jojolaptech.camel.repository.mysql.PayrollCalculationSettingRepository;

import com.jojolaptech.camel.repository.mysql.RequestmapRepository;

import com.jojolaptech.camel.repository.mysql.SecRoleRepository;

import com.jojolaptech.camel.repository.mysql.SecUserRepository;

import com.jojolaptech.camel.repository.mysql.TaxationRepository;

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

    private final CompanyProcessor companyProcessor;

    private final CompanyAddressProcessor companyAddressProcessor;

    private final BranchProcessor branchProcessor;

    private final BranchAddressProcessor branchAddressProcessor;

    private final FiscalYearProcessor fiscalYearProcessor;

    private final TaxationProcessor taxationProcessor;

    private final PayrollRuleProcessor payrollRuleProcessor;

    private final LeaveTypeProcessor leaveTypeProcessor;

    private final BranchLeaveTypeProcessor branchLeaveTypeProcessor;

    private final FiscalYearClosingParameterProcessor fiscalYearClosingParameterProcessor;

    private final AttParamsProcessor attParamsProcessor;

    private final DepartmentProcessor departmentProcessor;
    private final DepartmentParentLinkProcessor departmentParentLinkProcessor;

    private final UserProcessor userProcessor;

    private final UserDetailProcessor userDetailProcessor;

    private final RequestmapRepository requestmapRepository;

    private final SecRoleRepository secRoleRepository;

    private final CompanyRepository companyRepository;

    private final BranchRepository branchRepository;

    private final BranchDepartmentRepository branchDepartmentRepository;

    private final FiscalYearRepository fiscalYearRepository;

    private final TaxationRepository taxationRepository;

    private final PayrollCalculationSettingRepository payrollCalculationSettingRepository;

    private final CompanyFiscalYearClosingParameterRepository companyFiscalYearClosingParameterRepository;

    private final LeavesRepository leavesRepository;

    private final DepartmentRepository departmentRepository;

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

                    log.info("Starting HRM master migration...");

                    log.info("Start Time: {}", startDateTime.format(formatter));

                    log.info("Page Size: {}", PAGE_SIZE);

                    log.info("==========================================");

                })

                .to("direct:privilege-migration")

                .log("Step 1 completed: privilege-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:role-migration")

                .log("Step 2 completed: role-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-migration")

                .log("Step 3 completed: company-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:company-address-migration")

                .log("Step 4 completed: company-address-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-migration")

                .log("Step 5 completed: branch-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-address-migration")

                .log("Step 6 completed: branch-address-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:fiscal-year-migration")

                .log("Step 7 completed: fiscal-year-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:taxation-migration")

                .log("Step 8 completed: taxation-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:payroll-rule-migration")

                .log("Step 9 completed: payroll-rule-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:leave-type-migration")

                .log("Step 10 completed: leave-type-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:branch-leave-type-migration")

                .log("Step 11 completed: branch-leave-type-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:fy-closing-parameter-migration")

                .log("Step 12 completed: fy-closing-parameter-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:att-params-migration")

                .log("Step 13 completed: att-params-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:department-migration")

                .log("Step 14 completed: department-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:department-orphan-migration")

                .log("Step 15 completed: department-orphan-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:department-parent-link")

                .log("Step 16 completed: department-parent-link")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:user-migration")

                .log("Step 17 completed: user-migration")

                .process(exchange -> throttleBetweenSteps())

                .to("direct:user-detail-migration")

                .log("Step 18 completed: user-detail-migration")

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

                    int companyCount = exchange.getProperty("companyCount", 0, Integer.class);

                    int companyAddressCount = exchange.getProperty("companyAddressCount", 0, Integer.class);

                    int branchCount = exchange.getProperty("branchCount", 0, Integer.class);

                    int branchAddressCount = exchange.getProperty("branchAddressCount", 0, Integer.class);

                    int fiscalYearCount = exchange.getProperty("fiscalYearCount", 0, Integer.class);

                    int taxationCount = exchange.getProperty("taxationCount", 0, Integer.class);

                    int payrollRuleCount = exchange.getProperty("payrollRuleCount", 0, Integer.class);

                    int leaveTypeCount = exchange.getProperty("leaveTypeCount", 0, Integer.class);

                    int branchLeaveTypeCount = exchange.getProperty("branchLeaveTypeCount", 0, Integer.class);

                    int fyClosingParameterCount = exchange.getProperty("fyClosingParameterCount", 0, Integer.class);

                    int attParamsCount = exchange.getProperty("attParamsCount", 0, Integer.class);

                    int departmentCount = exchange.getProperty("departmentCount", 0, Integer.class);

                    int departmentOrphanCount = exchange.getProperty("departmentOrphanCount", 0, Integer.class);

                    int departmentParentLinkCount = exchange.getProperty("departmentParentLinkCount", 0, Integer.class);

                    int userCount = exchange.getProperty("userCount", 0, Integer.class);

                    int userDetailCount = exchange.getProperty("userDetailCount", 0, Integer.class);



                    log.info("==========================================");

                    log.info("Master migration completed!");

                    log.info("Start Time: {}", exchange.getProperty("startDateTime", LocalDateTime.class).format(formatter));

                    log.info("End Time: {}", endDateTime.format(formatter));

                    log.info("Total Time: {} hours, {} minutes, {} seconds, {} milliseconds",

                            hours, minutes, seconds, milliseconds);

                    log.info("--------------------------------------------");

                    log.info("1. privilege (requestmap -> permission):   {}", privilegeCount);

                    log.info("2. role (secRole -> role):                 {}", roleCount);

                    log.info("3. company (company -> company):           {}", companyCount);

                    log.info("4. company address -> hrm_company_address: {}", companyAddressCount);

                    log.info("5. branch (branch -> branch):              {}", branchCount);

                    log.info("6. branch address -> address:              {}", branchAddressCount);

                    log.info("7. fiscal year -> master/company/branch:   {}", fiscalYearCount);

                    log.info("8. taxation -> nepali_tax:                {}", taxationCount);

                    log.info("9. payroll settings -> payroll_rule:      {}", payrollRuleCount);

                    log.info("10. leaves -> hrm_leave_type:              {}", leaveTypeCount);

                    log.info("11. leaves -> hrm_branch_leave_type:       {}", branchLeaveTypeCount);

                    log.info("12. FY closing params -> checklist:         {}", fyClosingParameterCount);

                    log.info("13. company defaults / attParams:          {}", attParamsCount);

                    log.info("14. department (branchDepartment -> dept):  {}", departmentCount);

                    log.info("15. department orphan (department -> dept): {}", departmentOrphanCount);

                    log.info("16. department parent links:               {}", departmentParentLinkCount);

                    log.info("17. user (secUser -> users):               {}", userCount);

                    log.info("18. user detail (employee -> profile):   {}", userDetailCount);

                    log.info("--------------------------------------------");

                    log.info("GRAND TOTAL:                               {}",

                            privilegeCount + roleCount + companyCount + companyAddressCount + branchCount
                                    + branchAddressCount + fiscalYearCount + taxationCount + payrollRuleCount
                                    + leaveTypeCount + branchLeaveTypeCount + fyClosingParameterCount + attParamsCount
                                    + departmentCount + departmentOrphanCount + departmentParentLinkCount + userCount
                                    + userDetailCount);

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



        from("direct:company-migration")

                .routeId("company-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched company page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No company rows in this page, continuing...")

                        .otherwise()

                            .process(companyProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "company-migration", "companyCount"));



        from("direct:company-address-migration")

                .routeId("company-address-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No company rows for address migration in this page, continuing...")

                        .otherwise()

                            .process(companyAddressProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "company-address-migration", "companyAddressCount"));



        from("direct:branch-migration")

                .routeId("branch-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = branchRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched branch page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No branch rows in this page, continuing...")

                        .otherwise()

                            .process(branchProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "branch-migration", "branchCount"));



        from("direct:branch-address-migration")

                .routeId("branch-address-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = branchRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No branch rows for address migration in this page, continuing...")

                        .otherwise()

                            .process(branchAddressProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "branch-address-migration", "branchAddressCount"));



        from("direct:fiscal-year-migration")

                .routeId("fiscal-year-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = fiscalYearRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No fiscalYear rows in this page, continuing...")

                        .otherwise()

                            .process(fiscalYearProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "fiscal-year-migration", "fiscalYearCount"));



        from("direct:taxation-migration")

                .routeId("taxation-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = taxationRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No taxation rows in this page, continuing...")

                        .otherwise()

                            .process(taxationProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "taxation-migration", "taxationCount"));



        from("direct:payroll-rule-migration")

                .routeId("payroll-rule-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = payrollCalculationSettingRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No payrollCalculationSetting rows in this page, continuing...")

                        .otherwise()

                            .process(payrollRuleProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "payroll-rule-migration", "payrollRuleCount"));



        from("direct:leave-type-migration")

                .routeId("leave-type-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = leavesRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No leaves rows in this page, continuing...")

                        .otherwise()

                            .process(leaveTypeProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "leave-type-migration", "leaveTypeCount"));



        from("direct:branch-leave-type-migration")

                .routeId("branch-leave-type-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = leavesRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No leaves rows for branch assignment in this page, continuing...")

                        .otherwise()

                            .process(branchLeaveTypeProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "branch-leave-type-migration", "branchLeaveTypeCount"));



        from("direct:fy-closing-parameter-migration")

                .routeId("fy-closing-parameter-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyFiscalYearClosingParameterRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No companyFiscalYearClosingParameter rows in this page, continuing...")

                        .otherwise()

                            .process(fiscalYearClosingParameterProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "fy-closing-parameter-migration", "fyClosingParameterCount"));



        from("direct:att-params-migration")

                .routeId("att-params-migration")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = companyRepository.findAll(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No company rows for att params / defaults in this page, continuing...")

                        .otherwise()

                            .process(attParamsProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "att-params-migration", "attParamsCount"));



        from("direct:department-migration")

                .routeId("department-migration")

                .setProperty(DepartmentProcessor.MIGRATION_SOURCE).constant(DepartmentProcessor.SOURCE_BRANCH_DEPARTMENT)

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = branchDepartmentRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched branchDepartment page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No branchDepartment rows in this page, continuing...")

                        .otherwise()

                            .process(departmentProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "department-migration", "departmentCount"));



        from("direct:department-orphan-migration")

                .routeId("department-orphan-migration")

                .setProperty(DepartmentProcessor.MIGRATION_SOURCE).constant(DepartmentProcessor.SOURCE_ORPHAN)

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = departmentRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                        log.info("Fetched department page={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No department rows in this page, continuing...")

                        .otherwise()

                            .process(departmentProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "department-orphan-migration", "departmentOrphanCount"));



        from("direct:department-parent-link")

                .routeId("department-parent-link")

                .setProperty("page").constant(0)

                .setProperty("hasNext").constant(true)

                .setProperty("importCount").constant(0)

                .loopDoWhile(exchange -> Boolean.TRUE.equals(exchange.getProperty("hasNext", Boolean.class)))

                    .process(exchange -> {

                        int page = exchange.getProperty("page", Integer.class);

                        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

                        var resultPage = departmentRepository.findMigratable(pageable);

                        exchange.getMessage().setBody(resultPage.getContent());

                        exchange.setProperty("hasNext", resultPage.hasNext());

                        exchange.setProperty("page", page + 1);

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No department rows for parent-link in this page, continuing...")

                        .otherwise()

                            .process(departmentParentLinkProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "department-parent-link", "departmentParentLinkCount"));



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



        from("direct:user-detail-migration")

                .routeId("user-detail-migration")

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

                        log.info("Fetched secUser page for user detail={}, size={}, returnedRows={}, hasNext={}",

                                page, PAGE_SIZE, resultPage.getNumberOfElements(), resultPage.hasNext());

                    })

                    .choice()

                        .when(simple("${body.size} == 0"))

                            .log("No secUser rows in this page for user detail, continuing...")

                        .otherwise()

                            .process(userDetailProcessor)

                            .process(exchange -> addImported(exchange))

                    .end()

                .end()

                .process(exchange -> finishCount(exchange, "user-detail-migration", "userDetailCount"));

    }



    private static void throttleBetweenSteps() throws InterruptedException {

        System.gc();

        Thread.sleep(MIGRATION_THROTTLE_MS);

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

