package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.EmployeeSecUser;
import com.jojolaptech.camel.model.mysql.SecUser;
import com.jojolaptech.camel.model.postgres.user.UserDetailEntity;
import com.jojolaptech.camel.model.postgres.user.UserEntity;
import com.jojolaptech.camel.repository.mysql.EmployeeSecUserRepository;
import com.jojolaptech.camel.repository.postgres.user.PgUserDetailRepository;
import com.jojolaptech.camel.repository.postgres.user.PgUserRepository;
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
public class UserDetailProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(UserDetailProcessor.class);

    private final PgUserRepository userRepository;
    private final PgUserDetailRepository userDetailRepository;
    private final EmployeeSecUserRepository employeeSecUserRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {
        List<SecUser> batch = exchange.getMessage().getBody(List.class);
        if (batch == null || batch.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        List<Long> userIds = batch.stream().map(SecUser::getId).toList();
        Map<Long, UserEntity> usersByMysqlId = userRepository.findByMysqlIdIn(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getMysqlId, user -> user));
        if (usersByMysqlId.isEmpty()) {
            exchange.setProperty("batchImported", 0);
            return;
        }

        Set<Long> withDetail = userDetailRepository.findUserMysqlIdsWithDetail(userIds);
        Map<Long, Employee> employeeByUserId = employeeSecUserRepository.findByUserIdInWithEmployee(userIds).stream()
                .collect(Collectors.toMap(
                        link -> link.getUser().getId(),
                        EmployeeSecUser::getEmployee,
                        (left, right) -> left));

        List<UserDetailEntity> toSave = new ArrayList<>();
        for (SecUser source : batch) {
            if (withDetail.contains(source.getId())) {
                continue;
            }
            UserEntity user = usersByMysqlId.get(source.getId());
            if (user == null) {
                continue;
            }

            Employee employee = employeeByUserId.get(source.getId());
            UserDetailEntity detail = employee != null
                    ? UserDetailMapper.fromEmployee(employee, user)
                    : UserDetailMapper.fromEmail(user.getEmailAddress(), user);
            toSave.add(detail);
        }

        if (!toSave.isEmpty()) {
            userDetailRepository.saveAll(toSave);
            userDetailRepository.flush();
        }

        log.info("User detail batch imported {} of {} secUser rows", toSave.size(), batch.size());
        exchange.setProperty("batchImported", toSave.size());
    }
}
