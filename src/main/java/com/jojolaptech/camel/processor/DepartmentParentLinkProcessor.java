package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.BranchDepartment;
import com.jojolaptech.camel.model.mysql.Department;
import com.jojolaptech.camel.model.postgres.company.DepartmentEntity;
import com.jojolaptech.camel.repository.mysql.BranchDepartmentRepository;
import com.jojolaptech.camel.repository.postgres.company.PgDepartmentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentParentLinkProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(DepartmentParentLinkProcessor.class);

    private final BranchDepartmentRepository branchDepartmentRepository;
    private final PgDepartmentRepository departmentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Department> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<DepartmentEntity> updates = new ArrayList<>();
        for (Department source : batch) {
            if (source.getParentDepartment() == null) {
                continue;
            }
            List<BranchDepartment> links = branchDepartmentRepository.findByDepartment_Id(source.getId());
            if (links.isEmpty()) {
                continue;
            }
            for (BranchDepartment link : links) {
                Long branchMysqlId = link.getBranch().getId();
                departmentRepository.findByMysqlIdAndMysqlBranchId(source.getId(), branchMysqlId)
                        .ifPresent(child -> {
                            if (child.getParentDepartment() != null) {
                                return;
                            }
                            departmentRepository.findByMysqlIdAndMysqlBranchId(
                                            source.getParentDepartment().getId(), branchMysqlId)
                                    .ifPresent(parent -> {
                                        child.setParentDepartment(parent);
                                        child.setIsLeafDepartment(false);
                                        updates.add(child);
                                    });
                        });
            }
        }

        if (!updates.isEmpty()) {
            departmentRepository.saveAll(updates);
            departmentRepository.flush();
        }

        log.info("Department parent-link batch updated {} of {} rows", updates.size(), batch.size());
        exchange.setProperty("batchImported", updates.size());
    }
}
