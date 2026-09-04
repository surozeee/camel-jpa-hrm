package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.DeviceLogs;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceLogRepository;
import com.jojolaptech.camel.repository.postgres.company.PgAttendanceRepository;
import com.jojolaptech.camel.repository.postgres.company.PgEmployeeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceLogsProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(DeviceLogsProcessor.class);

    private final PgEmployeeRepository employeeRepository;
    private final PgAttendanceRepository attendanceRepository;
    private final PgAttendanceLogRepository attendanceLogRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<DeviceLogs> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        int imported = ResidualDeviceAttendanceLogMigrator.migrate(
                batch,
                DeviceLogs::getId,
                DeviceLogs::getEnrollId,
                DeviceLogs::getCheckTime,
                DeviceLogs::getCheckType,
                DeviceLogs::getMacId,
                DeviceLogs::getSensorId,
                DeviceLogs::getVerifyCode,
                DeviceLogs::getWorkCode,
                AttendancePunchMigrationMapper::deviceLogMysqlId,
                "Migrated from deviceLogs",
                "deviceLogs",
                employeeRepository,
                attendanceRepository,
                attendanceLogRepository,
                log);
        exchange.setProperty("batchImported", imported);
    }
}
