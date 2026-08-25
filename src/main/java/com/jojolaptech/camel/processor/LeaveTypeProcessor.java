package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Leaves;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.repository.postgres.company.PgLeaveTypeRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaveTypeProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(LeaveTypeProcessor.class);

    private final PgLeaveTypeRepository leaveTypeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<Leaves> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> existingIds = leaveTypeRepository.findMysqlIdsByMysqlIdIn(
                batch.stream().map(Leaves::getId).toList());
        Set<String> namesInUse = new HashSet<>(leaveTypeRepository.findExistingNamesLowerCase());

        List<LeaveTypeEntity> toSave = new ArrayList<>();
        for (Leaves source : batch) {
            if (existingIds.contains(source.getId())) {
                continue;
            }
            if (source.getCompany() == null) {
                log.warn("Skipping leave id={}, missing company", source.getId());
                continue;
            }
            if (Boolean.FALSE.equals(source.getIsActive())) {
                log.info("Skipping inactive leave id={}", source.getId());
                continue;
            }
            toSave.add(LeaveMigrationMapper.toLeaveType(source, namesInUse));
        }

        if (!toSave.isEmpty()) {
            leaveTypeRepository.saveAll(toSave);
        }

        exchange.setProperty("batchImported", toSave.size());
    }
}
