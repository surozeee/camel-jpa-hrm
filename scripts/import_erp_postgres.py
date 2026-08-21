#!/usr/bin/env python3
"""Copy ERP master/user/company entities into camel postgres packages."""

from __future__ import annotations

import re
from pathlib import Path

ERP = Path(r"d:\Projects\hrmsuite-erp")
OUT = Path(r"d:\Projects\camel-jpa-hrm\src\main\java\com\jojolaptech\camel\model\postgres")
BASE = "com.jojolaptech.camel.model.postgres"

SKIP_FILES = {
    "RevisionInfoEntity.java",
    "ErrorCodeEnum.java",
    "HrmSetupStepKey.java",
    "HrmSetupStepStatus.java",
    "HrmSetupGroup.java",
}

REPLACEMENTS = [
    ("com.jojolapatech.erp.core.entity.BaseAuditEntity", f"{BASE}.BaseAuditEntity"),
    ("com.jojolapatech.erp.core.entity.BaseEntity", f"{BASE}.BaseEntity"),
    ("com.jojolapatech.erp.core.enums.", f"{BASE}.enums."),
    ("com.jojolapatech.erp.master.entity.", f"{BASE}.master."),
    ("com.jojolapatech.erp.master.enums.", f"{BASE}.master.enums."),
    ("com.jojolapatech.erp.master.converter.", f"{BASE}.master."),
    ("com.jojolapatech.erp.user.entity.", f"{BASE}.user."),
    ("com.jojolapatech.erp.user.enums.", f"{BASE}.user.enums."),
    ("com.jojolapatech.erp.company.entity.", f"{BASE}.company."),
    ("com.jojolapatech.erp.company.enums.", f"{BASE}.company.enums."),
    ("com.jojolapatech.erp.organization.entity.", f"{BASE}.company."),
    ("com.jojolapatech.erp.organization.enums.", f"{BASE}.company.enums."),
    ("com.jojolapatech.erp.recruitment.enums.", f"{BASE}.company.enums."),
    ("com.jojolapatech.erp.employee.enums.", f"{BASE}.company.enums."),
]


def rewrite(text: str, package: str) -> str:
    text = re.sub(r"^package\s+[\w.]+;", f"package {package};", text, count=1, flags=re.M)
    for old, new in REPLACEMENTS:
        text = text.replace(old, new)
    text = re.sub(r"(?m)^import org\.hibernate\.envers\.[^;]+;\n", "", text)
    text = re.sub(r"@Audited(?:\([^)]*\))?\s*", "", text)
    text = re.sub(r"@NotAudited(?:\([^)]*\))?\s*", "", text)
    return text


def copy_file(src: Path, dest: Path, package: str) -> None:
    if src.name in SKIP_FILES:
        return
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(rewrite(src.read_text(encoding="utf-8"), package), encoding="utf-8")


def copy_dir(src_dir: Path, dest_dir: Path, package: str) -> int:
    count = 0
    if not src_dir.exists():
        return 0
    for src in src_dir.glob("*.java"):
        copy_file(src, dest_dir / src.name, package)
        if src.name not in SKIP_FILES:
            count += 1
    return count


def main() -> None:
    counts = {}

    core_enums = [
        "StatusEnum", "LanguageEnum", "CountryEnum", "ChannelEnum",
        "FiscalYearTypeEnum", "MonthTypeEnum", "MonthEnum", "CurrencyEnum",
    ]
    core_src = ERP / "Master-Service/src/main/java/com/jojolapatech/erp/core/enums"
    n = 0
    for name in core_enums:
        src = core_src / f"{name}.java"
        if src.exists():
            copy_file(src, OUT / "enums" / f"{name}.java", f"{BASE}.enums")
            n += 1
    counts["core_enums"] = n

    counts["master_entities"] = copy_dir(
        ERP / "Master-Service/src/main/java/com/jojolapatech/erp/master/entity",
        OUT / "master",
        f"{BASE}.master",
    )
    counts["master_enums"] = copy_dir(
        ERP / "Master-Service/src/main/java/com/jojolapatech/erp/master/enums",
        OUT / "master" / "enums",
        f"{BASE}.master.enums",
    )
    conv = ERP / "Master-Service/src/main/java/com/jojolapatech/erp/master/converter/CountryIso2AttributeConverter.java"
    copy_file(conv, OUT / "master" / "CountryIso2AttributeConverter.java", f"{BASE}.master")

    counts["user_entities"] = copy_dir(
        ERP / "User-Service/src/main/java/com/jojolapatech/erp/user/entity",
        OUT / "user",
        f"{BASE}.user",
    )
    counts["user_enums"] = copy_dir(
        ERP / "User-Service/src/main/java/com/jojolapatech/erp/user/enums",
        OUT / "user" / "enums",
        f"{BASE}.user.enums",
    )

    counts["company_entities"] = copy_dir(
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/company/entity",
        OUT / "company",
        f"{BASE}.company",
    )
    counts["company_enums"] = copy_dir(
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/company/enums",
        OUT / "company" / "enums",
        f"{BASE}.company.enums",
    )

    org_entities = [
        "CompanyEntity", "OrganizationEntity", "OrganizationTypeEntity",
        "OrganizationAddressEntity", "CompanyTypeEntity", "BranchEntity",
        "BranchAddressEntity",
    ]
    org_src = ERP / "User-Service/src/main/java/com/jojolapatech/erp/organization/entity"
    n = 0
    for name in org_entities:
        src = org_src / f"{name}.java"
        if src.exists():
            copy_file(src, OUT / "company" / f"{name}.java", f"{BASE}.company")
            n += 1
    counts["org_company_entities"] = n
    copy_file(
        ERP / "User-Service/src/main/java/com/jojolapatech/erp/organization/enums/CompanyStepperEnum.java",
        OUT / "company" / "enums" / "CompanyStepperEnum.java",
        f"{BASE}.company.enums",
    )

    extra_enums = [
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/recruitment/enums/PreEmploymentRequirementCategory.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/recruitment/enums/PreEmploymentCollectionKind.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/recruitment/enums/JoiningTaskOwnerType.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/recruitment/enums/JoiningTaskDepartment.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/employee/enums/LeaveVerificationMode.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/employee/enums/LeaveProcessingMode.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/employee/enums/LeaveCreditTimingEnum.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/employee/enums/LeaveApprovalSeparationPolicy.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/employee/enums/RosterShiftSlotEnum.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/employee/enums/RosterScheduleTypeEnum.java",
        ERP / "Company-Service/src/main/java/com/jojolapatech/erp/employee/enums/RosterScheduleStatusEnum.java",
    ]
    n = 0
    for src in extra_enums:
        if src.exists():
            copy_file(src, OUT / "company" / "enums" / src.name, f"{BASE}.company.enums")
            n += 1
    counts["extra_company_enums"] = n

    print(counts)


if __name__ == "__main__":
    main()
