package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.CompanyPayrollHeading;
import com.jojolaptech.camel.model.mysql.TemplatePayrollHeading;
import com.jojolaptech.camel.model.mysql.enums.PayrollHeadingType;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.CompanyEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchSalaryBreakdownRepository;
import com.jojolaptech.camel.repository.postgres.company.PgCompanyRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Seeds template system headings into each migrated company as breakdown lines when missing
 * (matched by companyId + lineName). Remarks note migratedFromTemplate.
 */
@Component
@RequiredArgsConstructor
public class HeadingTemplateSeedProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(HeadingTemplateSeedProcessor.class);

    /** Synthetic mysql_id space for template-seeded lines (avoids colliding with CPH / PMS). */
    static final long TEMPLATE_HEADING_MYSQL_ID_OFFSET = 6_000_000_000_000L;

    private final PgCompanyRepository companyRepository;
    private final PgBranchSalaryBreakdownRepository breakdownRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<TemplatePayrollHeading> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<CompanyEntity> companies = companyRepository.findAll().stream()
                .filter(c -> c.getMysqlId() != null)
                .toList();
        if (companies.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<UUID> companyIds = new HashSet<>();
        for (CompanyEntity company : companies) {
            companyIds.add(company.getId());
        }

        Map<UUID, Map<String, BranchSalaryBreakdownEntity>> existingByCompanyAndName = new HashMap<>();
        for (BranchSalaryBreakdownEntity row : breakdownRepository.findByCompanyIdIn(companyIds)) {
            if (row.getLineName() == null) {
                continue;
            }
            existingByCompanyAndName
                    .computeIfAbsent(row.getCompanyId(), k -> new HashMap<>())
                    .putIfAbsent(row.getLineName().trim().toLowerCase(Locale.ROOT), row);
        }

        List<BranchSalaryBreakdownEntity> toSave = new ArrayList<>();
        Set<Long> usedMysqlIds = new HashSet<>();

        for (TemplatePayrollHeading source : batch) {
            if (source.getPayrollHeading() == null || source.getPayrollTemplate() == null) {
                continue;
            }
            if (source.getPayrollHeading().getHeadingType() == PayrollHeadingType.PARENT) {
                continue;
            }
            String lineName = PayrollHeadingMigrationMapper.trimToNull(source.getPayrollHeading().getHeadingName());
            if (lineName == null) {
                lineName = "TemplateHeading-" + source.getPayrollHeading().getId();
            }
            String templateName = source.getPayrollTemplate().getTemplateName();
            String lineKey = lineName.toLowerCase(Locale.ROOT);

            CompanyPayrollHeading synthetic = new CompanyPayrollHeading();
            synthetic.setId(source.getId());
            synthetic.setStatus(true);
            synthetic.setPayrollHeading(source.getPayrollHeading());

            for (CompanyEntity company : companies) {
                Map<String, BranchSalaryBreakdownEntity> byName =
                        existingByCompanyAndName.computeIfAbsent(company.getId(), k -> new HashMap<>());
                if (byName.containsKey(lineKey)) {
                    continue;
                }

                long mysqlId = TEMPLATE_HEADING_MYSQL_ID_OFFSET
                        + (company.getMysqlId() * 1_000_000L)
                        + source.getId();
                if (usedMysqlIds.contains(mysqlId)) {
                    continue;
                }

                BranchSalaryBreakdownEntity entity =
                        PayrollHeadingMigrationMapper.fromCompanyHeading(synthetic, company.getId());
                entity.setMysqlId(mysqlId);
                entity.setLineName(lineName);
                entity.setRemarks("migratedFromTemplate=" + templateName);

                toSave.add(entity);
                byName.put(lineKey, entity);
                usedMysqlIds.add(mysqlId);
            }
        }

        if (!toSave.isEmpty()) {
            breakdownRepository.saveAll(toSave);
        }
        exchange.setProperty("batchImported", toSave.size());
        if (!toSave.isEmpty()) {
            log.info("Heading template seed created {} breakdown lines in this batch", toSave.size());
        }
    }
}
