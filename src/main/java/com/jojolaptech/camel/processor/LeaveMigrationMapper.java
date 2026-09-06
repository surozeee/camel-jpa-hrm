package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Leaves;
import com.jojolaptech.camel.model.mysql.enums.LeaveCategory;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import java.util.Locale;
import java.util.Set;

final class LeaveMigrationMapper {

    private LeaveMigrationMapper() {
    }

    static LeaveTypeEntity toLeaveType(Leaves source, Set<String> namesInUse) {
        String baseName = FiscalMigrationMapper.trimToNull(source.getLeaveName());
        if (baseName == null) {
            baseName = "Leave-" + source.getId();
        }
        String uniqueName = uniqueName(baseName, source.getCompany().getId(), source.getId(), namesInUse);
        int maxDays = (int) Math.round(source.getMaxDay());

        LeaveTypeEntity entity = LeaveTypeEntity.builder()
                .mysqlId(source.getId())
                .name(uniqueName)
                .code("L-" + source.getId())
                .description("Migrated from legacy leaves id=" + source.getId())
                .isPaid(isPaid(source.getLeaveCategory()))
                .requiresApproval(true)
                .requiresMedicalCertificate(false)
                .maxDaysPerYear(maxDays > 0 ? maxDays : null)
                .maxDaysPerRequest(maxDays > 0 ? maxDays : null)
                .canCarryForward(true)
                .displayOrder(source.getId().intValue())
                .build();
        // Keep inactive legacy types so historical leave apps/balances can resolve FKs.
        entity.setStatus(Boolean.FALSE.equals(source.getIsActive()) ? StatusEnum.INACTIVE : StatusEnum.ACTIVE);
        return entity;
    }

    static String branchLeaveKey(Long branchMysqlId, Long leaveMysqlId) {
        return branchMysqlId + ":" + leaveMysqlId;
    }

    private static String uniqueName(String baseName, Long companyMysqlId, Long leaveMysqlId, Set<String> namesInUse) {
        String candidate = baseName;
        if (namesInUse.add(candidate.toLowerCase(Locale.ROOT))) {
            return candidate;
        }
        candidate = baseName + " (C" + companyMysqlId + ")";
        if (namesInUse.add(candidate.toLowerCase(Locale.ROOT))) {
            return candidate;
        }
        candidate = baseName + " (C" + companyMysqlId + "-" + leaveMysqlId + ")";
        namesInUse.add(candidate.toLowerCase(Locale.ROOT));
        return candidate;
    }

    private static boolean isPaid(LeaveCategory category) {
        return category == null || category == LeaveCategory.Paid;
    }
}
