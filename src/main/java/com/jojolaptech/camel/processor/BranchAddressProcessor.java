package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Branch;
import com.jojolaptech.camel.model.postgres.company.BranchAddressEntity;
import com.jojolaptech.camel.model.postgres.company.BranchEntity;
import com.jojolaptech.camel.repository.postgres.company.PgBranchRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BranchAddressProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(BranchAddressProcessor.class);

    private final PgBranchRepository branchRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Branch> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<Long> branchIds = batch.stream().map(Branch::getId).toList();
        Map<Long, BranchEntity> branchesByMysqlId = branchRepository.findByMysqlIdIn(branchIds).stream()
                .collect(Collectors.toMap(BranchEntity::getMysqlId, branch -> branch));
        Set<Long> withAddress = branchesByMysqlId.values().stream()
                .filter(branch -> branch.getBranchAddress() != null)
                .map(BranchEntity::getMysqlId)
                .collect(Collectors.toSet());

        List<BranchEntity> toSave = new ArrayList<>();
        for (Branch source : batch) {
            if (withAddress.contains(source.getId())) {
                continue;
            }
            if (!OrgMigrationMapper.hasBranchAddress(source)) {
                continue;
            }
            BranchEntity branch = branchesByMysqlId.get(source.getId());
            if (branch == null) {
                log.warn("Skipping branch address id={}, branch not migrated yet", source.getId());
                continue;
            }
            BranchAddressEntity address = AddressMigrationMapper.branchAddress(source);
            if (address == null) {
                continue;
            }
            branch.setBranchAddress(address);
            toSave.add(branch);
        }

        if (!toSave.isEmpty()) {
            branchRepository.saveAll(toSave);
            branchRepository.flush();
        }

        log.info("Branch address batch imported {} of {} rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }
}
