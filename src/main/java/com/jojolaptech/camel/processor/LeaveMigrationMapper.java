package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Leaves;
import com.jojolaptech.camel.model.mysql.enums.LeaveCategory;
import com.jojolaptech.camel.model.postgres.company.LeaveTypeEntity;
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
        String uniqueName = uniqueName(baseName, source.getCompany().getId(), namesInUse);
        int maxDays = (int) Math.round(source.getMaxDay());

        return LeaveTypeEntity.builder()
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
    }

    static String branchLeaveKey(Long branchMysqlId, Long leaveMysqlId) {
        return branchMysqlId + ":" + leaveMysqlId;
    }

    private static String uniqueName(String baseName, Long companyMysqlId, Set<String> namesInUse) {
        String candidate = baseName;
        String key = candidate.toLowerCase(Locale.ROOT);
        if (namesInUse.add(key)) {
            return candidate;
        }
        candidate = baseName + " (C" + companyMysqlId + ")";
        namesInUse.add(candidate.toLowerCase(Locale.ROOT));
        return candidate;
    }

    private static boolean isPaid(LeaveCategory category) {
        return category == null || category == LeaveCategory.Paid;
    }
}
