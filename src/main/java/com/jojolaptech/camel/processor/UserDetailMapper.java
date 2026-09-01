package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.Employee;
import com.jojolaptech.camel.model.mysql.enums.EmployeeTitle;
import com.jojolaptech.camel.model.postgres.enums.ChannelEnum;
import com.jojolaptech.camel.model.postgres.enums.CountryEnum;
import com.jojolaptech.camel.model.postgres.enums.LanguageEnum;
import com.jojolaptech.camel.model.postgres.enums.StatusEnum;
import com.jojolaptech.camel.model.postgres.user.UserDetailEntity;
import com.jojolaptech.camel.model.postgres.user.UserEntity;
import com.jojolaptech.camel.model.postgres.user.enums.GenderEnum;
import com.jojolaptech.camel.model.postgres.user.enums.SalutationEnum;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class UserDetailMapper {

    private static final LanguageEnum DEFAULT_LANGUAGE = LanguageEnum.EN;
    private static final CountryEnum DEFAULT_COUNTRY = CountryEnum.NP;
    private static final ChannelEnum DEFAULT_NOTIFY = ChannelEnum.EMAIL;

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
                .phoneNumber(firstNonBlank(trimToNull(employee.getPhone()), trimToNull(user.getMobileNumber())))
                .photoUrl(trimToNull(employee.getPhoto()))
                .language(resolveLanguage(employee))
                .country(DEFAULT_COUNTRY)
                .notifyTo(DEFAULT_NOTIFY)
                .enable2FA(false)
                .build();
        detail.setStatus(StatusEnum.ACTIVE);
        return detail;
    }

    static UserDetailEntity fromUser(UserEntity user) {
        ParsedName parsed = parseNameFromEmail(user.getEmailAddress());
        UserDetailEntity detail = UserDetailEntity.builder()
                .user(user)
                .firstName(parsed.firstName())
                .lastName(parsed.lastName())
                .name(parsed.displayName())
                .phoneNumber(trimToNull(user.getMobileNumber()))
                .language(DEFAULT_LANGUAGE)
                .country(DEFAULT_COUNTRY)
                .notifyTo(DEFAULT_NOTIFY)
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

    private static LanguageEnum resolveLanguage(Employee employee) {
        if (containsNepaliHint(employee.getMotherTongue()) || trimToNull(employee.getNepaliName()) != null) {
            return LanguageEnum.NE;
        }
        return DEFAULT_LANGUAGE;
    }

    private static boolean containsNepaliHint(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("nepali") || normalized.contains("nepal");
    }

    private record ParsedName(String firstName, String lastName, String displayName) {}

    private static ParsedName parseNameFromEmail(String email) {
        String localPart = extractLocalPart(email);
        String[] parts = localPart.split("[\\s._-]+");
        if (parts.length >= 2) {
            String first = capitalize(parts[0]);
            String last = capitalize(parts[parts.length - 1]);
            return new ParsedName(first, last, first + " " + last);
        }
        return new ParsedName(localPart, null, localPart);
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

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() == 1) {
            return value.toUpperCase(Locale.ROOT);
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String firstNonBlank(String first, String second) {
        return first != null ? first : second;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
