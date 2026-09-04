package com.jojolaptech.camel.processor;

import com.jojolaptech.camel.model.mysql.EmployeePayrollHeadingPayment;
import com.jojolaptech.camel.model.mysql.EmployeePayrollPayment;
import com.jojolaptech.camel.model.mysql.HeadingWisePayrollPayment;
import com.jojolaptech.camel.model.mysql.PayPeriod;
import com.jojolaptech.camel.model.mysql.PayrollHeading;
import com.jojolaptech.camel.model.mysql.PayrollSystemHeading;
import com.jojolaptech.camel.model.mysql.PayrollTransaction;
import com.jojolaptech.camel.model.mysql.enums.PayrollHeadingType;
import com.jojolaptech.camel.model.postgres.company.BranchEmployeeMonthWiseSalaryEntity;
import com.jojolaptech.camel.model.postgres.company.BranchSalaryBreakdownEntity;
import com.jojolaptech.camel.model.postgres.company.EmployeeEntity;
import com.jojolaptech.camel.model.postgres.company.PayrollCalculationSnapshot;
import com.jojolaptech.camel.model.postgres.company.enums.SalaryBreakdownLineTypeEnum;
import com.jojolaptech.camel.model.postgres.enums.CurrencyEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class PayrollPaymentHistoryMapper {

    /** Distinguishes PMS payrollTransaction aggregates from employeePayrollPayment ids. */
    static final long TRANSACTION_MYSQL_ID_OFFSET = 7_000_000_000_000L;

    private PayrollPaymentHistoryMapper() {
    }

    static LocalDate periodDate(PayPeriod period) {
        if (period == null) {
            return null;
        }
        LocalDate end = PayrollHeadingMigrationMapper.toLocalDate(period.getEndDate());
        if (end != null) {
            return end;
        }
        return PayrollHeadingMigrationMapper.toLocalDate(period.getStartDate());
    }

    static int salaryYear(LocalDate date) {
        return date != null ? date.getYear() : LocalDate.now().getYear();
    }

    static int salaryMonth(LocalDate date) {
        return date != null ? date.getMonthValue() : LocalDate.now().getMonthValue();
    }

    static long transactionAggregateMysqlId(long payrollMonthId, long employeeMysqlId) {
        // Stable unique key within offset space (month << 20 | employee low bits)
        return TRANSACTION_MYSQL_ID_OFFSET + (payrollMonthId * 1_000_000L) + (employeeMysqlId % 1_000_000L);
    }

    static BranchEmployeeMonthWiseSalaryEntity fromPayment(
            EmployeePayrollPayment payment,
            EmployeeEntity employee,
            UUID branchId,
            List<PayrollCalculationSnapshot.SnapshotLine> lines) {

        LocalDate period = periodDate(payment.getPaymentPeriod());
        int year = salaryYear(period);
        int month = salaryMonth(period);

        BigDecimal basic = sumBasic(lines);
        BigDecimal allowances = sumEarnings(lines).subtract(basic);
        if (allowances.compareTo(BigDecimal.ZERO) < 0) {
            allowances = BigDecimal.ZERO;
        }
        BigDecimal deductions = sumDeductions(lines);
        BigDecimal tax = nullToZero(payment.getTaxAmount());
        BigDecimal net = nullToZero(payment.getNetAmount());
        BigDecimal gross = sumEarnings(lines);
        if (gross.compareTo(BigDecimal.ZERO) == 0) {
            gross = net.add(tax).add(deductions);
        }

        PayrollCalculationSnapshot snapshot = PayrollCalculationSnapshot.builder()
                .snapshotVersion(1)
                .calculationDetailsComplete(!lines.isEmpty())
                .engineVersion("legacy-migrate")
                .clientRequestId("mysql-payment-" + payment.getId())
                .salaryYear(year)
                .salaryMonth(month)
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .basicSalary(basic)
                .grossSalary(gross)
                .totalAllowances(allowances)
                .taxableIncome(nullToZero(payment.getTaxableAmount()))
                .annualTaxableIncome(nullToZero(payment.getYearlyTaxableAmount()))
                .incomeTax(tax)
                .totalDeductions(deductions)
                .netSalary(net)
                .lines(lines)
                .legacyComponentBackfill(true)
                .build();

        return BranchEmployeeMonthWiseSalaryEntity.builder()
                .mysqlId(payment.getId())
                .branchId(branchId)
                .employeeId(employee.getId())
                .salaryYear(year)
                .salaryMonth(month)
                .grossSalary(gross)
                .netSalary(net)
                .basicSalary(basic)
                .totalAllowances(allowances)
                .totalDeductions(deductions)
                .totalTax(tax)
                .taxFreeRedemption(BigDecimal.ZERO)
                .currency(CurrencyEnum.NPR)
                .isProcessed(payment.getVerifiedDate() != null || Boolean.TRUE.equals(payment.getStatus()))
                .isPaid(payment.getPaidDate() != null)
                .clientRequestId("mysql-payment-" + payment.getId())
                .calculationEngineVersion("legacy-migrate")
                .calculationSnapshot(snapshot)
                .description(payment.getComment())
                .remarks("migrated from employeePayrollPayment#" + payment.getId()
                        + (payment.getPaymentMethod() != null ? " method=" + payment.getPaymentMethod() : ""))
                .build();
    }

    static BranchEmployeeMonthWiseSalaryEntity fromTransactions(
            long aggregateMysqlId,
            EmployeeEntity employee,
            UUID branchId,
            int year,
            int month,
            List<PayrollTransaction> txs,
            Map<String, BranchSalaryBreakdownEntity> breakdownByLineName) {

        List<PayrollCalculationSnapshot.SnapshotLine> lines = new ArrayList<>();
        BigDecimal earnings = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        BigDecimal basic = BigDecimal.ZERO;

        List<PayrollTransaction> sorted = txs.stream()
                .sorted(Comparator.comparing(t -> t.getPriority() == null ? 0 : t.getPriority()))
                .toList();

        int order = 0;
        for (PayrollTransaction tx : sorted) {
            PayrollHeading heading = tx.getPayrollheading();
            String lineName = heading != null && heading.getTitle() != null
                    ? heading.getTitle().trim()
                    : (tx.getTitle() != null ? tx.getTitle().trim() : "Line");
            BranchSalaryBreakdownEntity breakdown = breakdownByLineName.get(normalize(lineName));
            SalaryBreakdownLineTypeEnum lineType = breakdown != null
                    ? breakdown.getLineType()
                    : inferLineTypeFromTitle(lineName);
            boolean isBasic = breakdown != null && Boolean.TRUE.equals(breakdown.getIsBasicSalaryLine())
                    || lineName.toLowerCase(Locale.ROOT).contains("basic");
            BigDecimal amount = BigDecimal.valueOf(tx.getAmount());

            lines.add(PayrollCalculationSnapshot.SnapshotLine.builder()
                    .branchSalaryBreakdownId(breakdown != null ? breakdown.getId() : null)
                    .lineName(lineName)
                    .lineType(lineType)
                    .amount(amount)
                    .displayOrder(tx.getPriority() != null ? tx.getPriority() : order++)
                    .isBasicSalaryLine(isBasic)
                    .rateType(breakdown != null ? breakdown.getRateType() : null)
                    .remarks(tx.getRemarks())
                    .status("MIGRATED")
                    .build());

            if (lineType == SalaryBreakdownLineTypeEnum.DEDUCTION) {
                deductions = deductions.add(amount);
            } else {
                earnings = earnings.add(amount);
                if (isBasic) {
                    basic = basic.add(amount);
                }
            }
        }

        BigDecimal allowances = earnings.subtract(basic);
        if (allowances.compareTo(BigDecimal.ZERO) < 0) {
            allowances = BigDecimal.ZERO;
        }
        BigDecimal net = earnings.subtract(deductions);

        PayrollCalculationSnapshot snapshot = PayrollCalculationSnapshot.builder()
                .snapshotVersion(1)
                .calculationDetailsComplete(true)
                .engineVersion("legacy-migrate-pms")
                .clientRequestId("mysql-tx-" + aggregateMysqlId)
                .salaryYear(year)
                .salaryMonth(month)
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .basicSalary(basic)
                .grossSalary(earnings)
                .totalAllowances(allowances)
                .totalDeductions(deductions)
                .netSalary(net)
                .lines(lines)
                .legacyComponentBackfill(true)
                .build();

        return BranchEmployeeMonthWiseSalaryEntity.builder()
                .mysqlId(aggregateMysqlId)
                .branchId(branchId)
                .employeeId(employee.getId())
                .salaryYear(year)
                .salaryMonth(month)
                .grossSalary(earnings)
                .netSalary(net)
                .basicSalary(basic)
                .totalAllowances(allowances)
                .totalDeductions(deductions)
                .totalTax(BigDecimal.ZERO)
                .taxFreeRedemption(BigDecimal.ZERO)
                .currency(CurrencyEnum.NPR)
                .isProcessed(true)
                .isPaid(true)
                .clientRequestId("mysql-tx-" + aggregateMysqlId)
                .calculationEngineVersion("legacy-migrate-pms")
                .calculationSnapshot(snapshot)
                .remarks("migrated from payrollTransaction aggregate (" + txs.size() + " lines)")
                .build();
    }

    static List<PayrollCalculationSnapshot.SnapshotLine> linesFromHeadingWise(
            List<HeadingWisePayrollPayment> headingPayments,
            Map<String, BranchSalaryBreakdownEntity> breakdownByLineName) {
        List<PayrollCalculationSnapshot.SnapshotLine> lines = new ArrayList<>();
        int order = 0;
        for (HeadingWisePayrollPayment hp : headingPayments) {
            PayrollSystemHeading system = hp.getPayrollHeading();
            if (system == null || system.getHeadingType() == PayrollHeadingType.PARENT) {
                continue;
            }
            String lineName = system.getHeadingName() != null ? system.getHeadingName().trim() : "Heading";
            BranchSalaryBreakdownEntity breakdown = breakdownByLineName.get(normalize(lineName));
            SalaryBreakdownLineTypeEnum lineType = mapSystemType(system.getHeadingType());
            boolean isBasic = system.getHeadingType() == PayrollHeadingType.BASIC_AMOUNT;
            lines.add(PayrollCalculationSnapshot.SnapshotLine.builder()
                    .branchSalaryBreakdownId(breakdown != null ? breakdown.getId() : null)
                    .lineName(lineName)
                    .lineType(lineType)
                    .amount(nullToZero(hp.getAmount()))
                    .displayOrder(system.getSortingNo() != null ? system.getSortingNo() : order++)
                    .isBasicSalaryLine(isBasic)
                    .rateType(breakdown != null ? breakdown.getRateType() : null)
                    .rateValue(breakdown != null ? breakdown.getRateValue() : null)
                    .percentBase(breakdown != null ? breakdown.getPercentBase() : null)
                    .status("MIGRATED")
                    .build());
        }
        return lines;
    }

    static List<PayrollCalculationSnapshot.SnapshotLine> linesFromEmployeeHeadingPayments(
            List<EmployeePayrollHeadingPayment> headingPayments,
            Map<String, BranchSalaryBreakdownEntity> breakdownByLineName) {
        List<PayrollCalculationSnapshot.SnapshotLine> lines = new ArrayList<>();
        int order = 0;
        for (EmployeePayrollHeadingPayment hp : headingPayments) {
            PayrollSystemHeading system = hp.getPayrollHeading();
            if (system == null || system.getHeadingType() == PayrollHeadingType.PARENT) {
                continue;
            }
            String lineName = system.getHeadingName() != null ? system.getHeadingName().trim() : "Heading";
            BranchSalaryBreakdownEntity breakdown = breakdownByLineName.get(normalize(lineName));
            lines.add(PayrollCalculationSnapshot.SnapshotLine.builder()
                    .branchSalaryBreakdownId(breakdown != null ? breakdown.getId() : null)
                    .lineName(lineName)
                    .lineType(mapSystemType(system.getHeadingType()))
                    .amount(nullToZero(hp.getAmount()))
                    .displayOrder(system.getSortingNo() != null ? system.getSortingNo() : order++)
                    .isBasicSalaryLine(system.getHeadingType() == PayrollHeadingType.BASIC_AMOUNT)
                    .rateType(breakdown != null ? breakdown.getRateType() : null)
                    .status("MIGRATED")
                    .build());
        }
        return lines;
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static SalaryBreakdownLineTypeEnum mapSystemType(PayrollHeadingType type) {
        if (type == null) {
            return SalaryBreakdownLineTypeEnum.EARNING;
        }
        return switch (type) {
            case DEDUCTION, DEDUCTION_AT, EXEMPTED_DEDUCTION -> SalaryBreakdownLineTypeEnum.DEDUCTION;
            default -> SalaryBreakdownLineTypeEnum.EARNING;
        };
    }

    private static SalaryBreakdownLineTypeEnum inferLineTypeFromTitle(String lineName) {
        String n = lineName.toLowerCase(Locale.ROOT);
        if (n.contains("deduc") || n.contains("tax") || n.contains("pf") || n.contains("cit") || n.contains("ssf")) {
            return SalaryBreakdownLineTypeEnum.DEDUCTION;
        }
        return SalaryBreakdownLineTypeEnum.EARNING;
    }

    private static BigDecimal sumEarnings(List<PayrollCalculationSnapshot.SnapshotLine> lines) {
        return lines.stream()
                .filter(l -> l.getLineType() != SalaryBreakdownLineTypeEnum.DEDUCTION)
                .map(l -> nullToZero(l.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumDeductions(List<PayrollCalculationSnapshot.SnapshotLine> lines) {
        return lines.stream()
                .filter(l -> l.getLineType() == SalaryBreakdownLineTypeEnum.DEDUCTION)
                .map(l -> nullToZero(l.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumBasic(List<PayrollCalculationSnapshot.SnapshotLine> lines) {
        return lines.stream()
                .filter(l -> Boolean.TRUE.equals(l.getIsBasicSalaryLine()))
                .map(l -> nullToZero(l.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
