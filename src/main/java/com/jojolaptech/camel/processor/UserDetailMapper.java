package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.enums.EmployeeTitle;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.UserDetailEntity;
import com.jojolaptech.camel.model.postgres.user.UserEntity;
import com.jojolaptech.camel.model.postgres.user.enums.GenderEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SalutationEnum;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class UserDetailMapper {

    private UserDetailMapper() {}

    static UserDetailEntity fromEmployee(Employee employee, UserEntity user) {
        String firstName = trimToNull(employee.getName());
        String middleName = trimToNull(employee.getMiddleName());
        String lastName = trimToNull(employee.getLastname());
        UserDetailEntity detail = UserDetailEntity.builder()
                .user(user)
                .salutation(mapSalutation(employee.getTitle()))
                .gender(mapGender(employee.getGender()))
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .name(buildDisplayName(firstName, middleName, lastName))
                .phoneNumber(trimToNull(employee.getPhone()))
                .photoUrl(trimToNull(employee.getPhoto()))
                .enable2FA(false)
                .build();
        detail.setStatus(StatusEnum.ACTIVE);
        return detail;
    }

    static UserDetailEntity fromEmail(String email, UserEntity user) {
        String localPart = extractLocalPart(email);
        UserDetailEntity detail = UserDetailEntity.builder()
                .user(user)
                .firstName(localPart)
                .name(localPart)
                .enable2FA(false)
                .build();
        detail.setStatus(StatusEnum.ACTIVE);
        return detail;
    }

    static String buildDisplayName(String firstName, String middleName, String lastName) {
        String displayName = Stream.of(firstName, middleName, lastName)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));
        return displayName.isBlank() ? null : displayName;
    }

    static GenderEnum mapGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        String normalized = gender.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "M", "MALE", "MAN" -> GenderEnum.MALE;
            case "F", "FEMALE", "WOMAN" -> GenderEnum.FEMALE;
            default -> GenderEnum.OTHER;
        };
    }

    static SalutationEnum mapSalutation(EmployeeTitle title) {
        if (title == null) {
            return null;
        }
        return switch (title) {
            case Mr -> SalutationEnum.MR;
            case Mrs -> SalutationEnum.MRS;
            case Miss -> SalutationEnum.MS;
        };
    }

    private static String extractLocalPart(String email) {
        if (email == null || email.isBlank()) {
            return "User";
        }
        int at = email.indexOf('@');
        String localPart = at > 0 ? email.substring(0, at) : email;
        localPart = localPart.replace('.', ' ').replace('_', ' ').trim();
        return localPart.isBlank() ? "User" : localPart;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
