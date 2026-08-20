#!/usr/bin/env python3
"""Convert Grails GORM domain classes to Spring JPA entities."""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

DOMAIN_ROOT = Path(r"d:\Projects\hrmsuite\grails-app\domain")
ENUM_ROOTS = [
    Path(r"d:\Projects\hrmsuite\grails-app\utils\enums"),
    Path(r"d:\Projects\hrmsuite\grails-app\enums"),
    Path(r"d:\Projects\hrmsuite\src\main\groovy\enums"),
]
OUT_ROOT = Path(r"d:\Projects\camel-jpa-hrm\src\main\java\com\jojolaptech\camel\model\mysql")
BASE_PKG = "com.jojolaptech.camel.model.mysql"
ENUM_PKG = f"{BASE_PKG}.enums"

JAVA_KEYWORDS = {
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
    "class", "const", "continue", "default", "do", "double", "else", "enum",
    "extends", "final", "finally", "float", "for", "goto", "if", "implements",
    "import", "instanceof", "int", "interface", "long", "native", "new",
    "package", "private", "protected", "public", "return", "short", "static",
    "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
    "transient", "try", "void", "volatile", "while", "true", "false", "null",
}

PRIMITIVES = {
    "String": "String",
    "Date": "java.util.Date",
    "Time": "java.sql.Time",
    "Timestamp": "java.sql.Timestamp",
    "Boolean": "Boolean",
    "boolean": "boolean",
    "Integer": "Integer",
    "int": "int",
    "Long": "Long",
    "long": "long",
    "Double": "Double",
    "double": "double",
    "Float": "Float",
    "float": "float",
    "BigDecimal": "java.math.BigDecimal",
    "BigInteger": "java.math.BigInteger",
    "Byte": "Byte",
    "byte": "byte",
    "Short": "Short",
    "short": "short",
    "Character": "Character",
    "char": "char",
}

WRAPPER_FOR_PRIMITIVE = {
    "int": "Integer",
    "long": "Long",
    "boolean": "Boolean",
    "double": "Double",
    "float": "Float",
    "short": "Short",
    "byte": "Byte",
    "char": "Character",
}

SKIP_STATIC = {
    "constraints", "mapping", "belongsTo", "hasMany", "hasOne", "transients",
    "mappedBy", "fetchMode", "auditable", "embedded", "hasMany",
}

STATIC_BLOCK_NAMES = (
    "constraints", "mapping", "belongsTo", "hasMany", "hasOne", "transients",
    "mappedBy", "fetchMode", "auditable",
)

USED_STANDALONE_ENUMS = {
    "StatusEnum", "BankTypeEnum", "AccountDeleteStatusEnum",
    "PaymentMethodEnum", "MessageCategory",
}


def decapitalize(name: str) -> str:
    if not name:
        return name
    if len(name) > 1 and name[0].isupper() and name[1].isupper():
        return name
    return name[0].lower() + name[1:]


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    lines = []
    for line in text.splitlines():
        if "//" in line:
            in_str = False
            out = []
            i = 0
            while i < len(line):
                ch = line[i]
                if ch in "\"'" and (i == 0 or line[i - 1] != "\\"):
                    in_str = not in_str
                    out.append(ch)
                    i += 1
                    continue
                if not in_str and ch == "/" and i + 1 < len(line) and line[i + 1] == "/":
                    break
                out.append(ch)
                i += 1
            line = "".join(out)
        lines.append(line)
    return "\n".join(lines)


def find_matching(text: str, open_idx: int, open_ch: str, close_ch: str) -> int:
    depth = 0
    i = open_idx
    in_str = None
    while i < len(text):
        ch = text[i]
        if in_str:
            if ch == "\\" and i + 1 < len(text):
                i += 2
                continue
            if ch == in_str:
                in_str = None
            i += 1
            continue
        if ch in ("'", '"'):
            in_str = ch
            i += 1
            continue
        if ch == open_ch:
            depth += 1
        elif ch == close_ch:
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def extract_block_after(text: str, keyword: str) -> str | None:
    m = re.search(rf"\bstatic\s+{keyword}\s*=", text)
    if not m:
        m = re.search(rf"\bstatic\s+{keyword}\s*\{{", text)
        if m:
            start = text.find("{", m.start())
            end = find_matching(text, start, "{", "}")
            return text[start + 1:end] if end != -1 else None
        return None
    i = m.end()
    while i < len(text) and text[i].isspace():
        i += 1
    if i >= len(text):
        return None
    if text[i] == "[":
        end = find_matching(text, i, "[", "]")
        return text[i + 1:end] if end != -1 else None
    if text[i] == "{":
        end = find_matching(text, i, "{", "}")
        return text[i + 1:end] if end != -1 else None
    m2 = re.match(r"([A-Za-z_][\w.]*)", text[i:])
    return m2.group(1) if m2 else None


def parse_map_entries(body: str) -> dict[str, str]:
    if not body:
        return {}
    body = body.strip()
    if re.fullmatch(r"[A-Za-z_][\w.]*", body):
        simple = body.split(".")[-1]
        return {decapitalize(simple): simple}
    entries = {}
    for m in re.finditer(r"(\w+)\s*:\s*([A-Za-z_][\w.]*)", body):
        entries[m.group(1)] = m.group(2).split(".")[-1]
    return entries


def parse_list_entries(body: str) -> list[str]:
    if not body:
        return []
    return re.findall(r"['\"](\w+)['\"]", body)


def parse_constraints_nullable(body: str) -> set[str]:
    nullable = set()
    if not body:
        return nullable
    for m in re.finditer(r"(\w+)\s+nullable\s*:\s*true", body):
        nullable.add(m.group(1))
    return nullable


def parse_constraints_unique(body: str) -> set[str]:
    unique = set()
    if not body:
        return unique
    for m in re.finditer(r"(\w+)\s+.*\bunique\s*:\s*true", body):
        unique.add(m.group(1))
    return unique


def parse_mapping(body: str) -> dict:
    info = {
        "yes_no": set(),
        "sql_types": {},
        "table": None,
        "version": True,
        "composite": [],
    }
    if not body:
        return info
    m = re.search(r"\btable\s+['\"](\w+)['\"]", body)
    if m:
        info["table"] = m.group(1)
    if re.search(r"\bversion\s+false\b", body):
        info["version"] = False
    m = re.search(r"id\s+composite\s*:\s*\[([^\]]+)\]", body)
    if m:
        info["composite"] = [x.strip().strip("'\"") for x in m.group(1).split(",")]
    for m in re.finditer(r"(\w+)\s+type\s*:\s*['\"]yes_no['\"]", body):
        info["yes_no"].add(m.group(1))
    for m in re.finditer(r"(\w+)\s+sqlType\s*:\s*['\"]([^'\"]+)['\"]", body):
        info["sql_types"][m.group(1)] = m.group(2)
    return info


def remove_static_blocks(text: str) -> str:
    result = text
    for name in STATIC_BLOCK_NAMES:
        while True:
            m = re.search(rf"\bstatic\s+{name}\b", result)
            if not m:
                break
            i = m.end()
            while i < len(result) and result[i] in " \t":
                i += 1
            if i < len(result) and result[i] == "=":
                i += 1
                while i < len(result) and result[i].isspace():
                    i += 1
            if i < len(result) and result[i] == "{":
                end = find_matching(result, i, "{", "}")
                result = result[:m.start()] + result[end + 1:] if end != -1 else result[:m.start()]
            elif i < len(result) and result[i] == "[":
                end = find_matching(result, i, "[", "]")
                result = result[:m.start()] + result[end + 1:] if end != -1 else result[:m.start()]
            else:
                rest = result[i:]
                m2 = re.match(r"[A-Za-z_][\w.]*", rest)
                end = i + (m2.end() if m2 else 0)
                result = result[:m.start()] + result[end:]
    return result


def extract_enum_constants(body: str, enum_name: str) -> list[str]:
    body = strip_comments(body)
    body = re.split(r"\b(?:final|public|private|protected|void)\b", body, maxsplit=1)[0]
    body = re.split(rf"\b{re.escape(enum_name)}\s*\(", body, maxsplit=1)[0]
    body = re.split(r"\b(?:String|int|boolean|def|Integer|Long)\s+\w+", body, maxsplit=1)[0]
    skip = {
        "enum", "value", "String", "Integer", "int", "boolean", "def",
        "toString", "getKey", "getValue", "name",
    }
    constants = []
    ident: list[str] = []
    in_str = None
    depth = 0
    i = 0
    while i < len(body):
        ch = body[i]
        if in_str:
            if ch == "\\" and i + 1 < len(body):
                i += 2
                continue
            if ch == in_str:
                in_str = None
            i += 1
            continue
        if ch in "\"'":
            in_str = ch
            i += 1
            continue
        if ch == "(":
            depth += 1
            i += 1
            continue
        if ch == ")":
            depth = max(0, depth - 1)
            i += 1
            continue
        if depth > 0:
            i += 1
            continue
        if ch.isalpha() or ch == "_" or (ident and ch.isdigit()):
            ident.append(ch)
            i += 1
            continue
        if ident:
            name = "".join(ident)
            ident = []
            if name not in skip:
                constants.append(name)
        i += 1
    if ident:
        name = "".join(ident)
        if name not in skip:
            constants.append(name)
    seen = set()
    out = []
    for c in constants:
        if c not in seen:
            seen.add(c)
            out.append(c)
    return out


@dataclass
class FieldInfo:
    type_name: str
    name: str
    default: str | None = None
    nullable: bool = False
    unique: bool = False
    yes_no: bool = False
    sql_type: str | None = None
    is_enum: bool = False
    is_entity: bool = False
    is_id: bool = False


@dataclass
class DomainInfo:
    groovy_package: str
    class_name: str
    fields: list[FieldInfo] = field(default_factory=list)
    belongs_to: dict[str, str] = field(default_factory=dict)
    has_one: dict[str, str] = field(default_factory=dict)
    transients: list[str] = field(default_factory=list)
    mapping: dict = field(default_factory=dict)
    local_enums: set[str] = field(default_factory=set)
    source: Path | None = None


def parse_fields(class_body: str, transients: set[str]) -> list[tuple[str, str, str | None]]:
    cleaned = remove_static_blocks(class_body)
    fields = []
    for raw in cleaned.splitlines():
        line = raw.strip()
        if not line or line.startswith("*"):
            continue
        if "(" in line:
            continue
        if line.startswith(("if ", "for ", "while ", "switch ", "return ", "new ", "log.", "print")):
            continue
        if re.match(r"^(public|protected|private|static|def)\b", line) and "static" in line:
            continue
        if line.startswith("def "):
            continue
        m = re.match(
            r"^(?:(?:public|protected|private)\s+)?([\w.]+(?:\s*<[^>]+>)?)\s+(\w+)\s*(?:=\s*(.+?))?\s*;?\s*$",
            line,
        )
        if not m:
            continue
        type_name, name, default = m.group(1).strip(), m.group(2), (m.group(3) or "").strip() or None
        if name in transients or name in SKIP_STATIC:
            continue
        if type_name in {"static", "def", "return", "import", "package", "class", "enum", "transient"}:
            continue
        if type_name in JAVA_KEYWORDS and type_name not in {
            "boolean", "byte", "char", "double", "float", "int", "long", "short",
        }:
            continue
        if name in {"springSecurityService"}:
            continue
        if name[0].isupper() and type_name[0].islower() and type_name not in PRIMITIVES:
            continue
        fields.append((type_name.split(".")[-1], name, default))
    return fields


def parse_domain(path: Path) -> tuple[DomainInfo | None, dict[str, list[str]]]:
    raw = path.read_text(encoding="utf-8", errors="ignore")
    text = strip_comments(raw)
    pkg_m = re.search(r"^package\s+([\w.]+)", text, re.M)
    groovy_pkg = pkg_m.group(1) if pkg_m else path.parent.name

    class_m = re.search(r"\bclass\s+(\w+)", text)
    if not class_m:
        return None, {}
    class_name = class_m.group(1)
    class_start = text.find("{", class_m.end())
    class_end = find_matching(text, class_start, "{", "}")
    class_body = text[class_start + 1:class_end] if class_end != -1 else text[class_start + 1:]

    enums = {}
    for em in re.finditer(r"\benum\s+(\w+)\s*\{", text):
        e_start = text.find("{", em.end() - 1)
        e_end = find_matching(text, e_start, "{", "}")
        body = text[e_start + 1:e_end] if e_end != -1 else ""
        enums[em.group(1)] = extract_enum_constants(body, em.group(1))

    belongs = parse_map_entries(extract_block_after(text, "belongsTo") or "")
    has_one = parse_map_entries(extract_block_after(text, "hasOne") or "")
    transients = parse_list_entries(extract_block_after(text, "transients") or "")
    mapping = parse_mapping(extract_block_after(text, "mapping") or "")
    constraints_body = extract_block_after(text, "constraints") or ""
    nullable = parse_constraints_nullable(constraints_body)
    unique = parse_constraints_unique(constraints_body)

    info = DomainInfo(
        groovy_package=groovy_pkg,
        class_name=class_name,
        belongs_to=belongs,
        has_one=has_one,
        transients=transients,
        mapping=mapping,
        local_enums=set(enums),
        source=path,
    )

    seen = set()
    for type_name, name, default in parse_fields(class_body, set(transients)):
        if name in seen:
            continue
        seen.add(name)
        info.fields.append(FieldInfo(
            type_name=type_name,
            name=name,
            default=default,
            nullable=name in nullable,
            unique=name in unique,
            yes_no=name in mapping["yes_no"],
            sql_type=mapping["sql_types"].get(name),
        ))

    for name, type_name in belongs.items():
        if name in seen:
            continue
        seen.add(name)
        info.fields.append(FieldInfo(
            type_name=type_name,
            name=name,
            nullable=name in nullable,
            unique=name in unique,
        ))

    return info, enums


def java_default(value: str | None, type_name: str) -> str | None:
    if not value:
        return None
    value = value.strip().rstrip(";")
    if value.startswith("Boolean."):
        return value
    if value in {"true", "false", "Boolean.TRUE", "Boolean.FALSE"}:
        return value
    if re.match(r"^-?\d+(\.\d+)?[dDfFlL]?$", value):
        return value
    if value.startswith(("'", '"')):
        return '"' + value.strip("'\"") + '"'
    if re.match(r"^[A-Za-z_][\w.]*$", value):
        return value
    return None


def write_enum(name: str, constants: list[str]) -> None:
    OUT_ROOT.joinpath("enums").mkdir(parents=True, exist_ok=True)
    const_body = ",\n    ".join(constants) if constants else "VALUE"
    content = (
        f"package {ENUM_PKG};\n\n"
        f"public enum {name} {{\n"
        f"    {const_body}\n"
        f"}}\n"
    )
    OUT_ROOT.joinpath("enums", f"{name}.java").write_text(content, encoding="utf-8")


def parse_standalone_enum(path: Path) -> tuple[str, list[str]] | None:
    text = strip_comments(path.read_text(encoding="utf-8", errors="ignore"))
    m = re.search(r"\benum\s+(\w+)\s*\{", text)
    if not m:
        return None
    name = m.group(1)
    if name not in USED_STANDALONE_ENUMS:
        return None
    start = text.find("{", m.end() - 1)
    end = find_matching(text, start, "{", "}")
    body = text[start + 1:end] if end != -1 else ""
    return name, extract_enum_constants(body, name)


def generate_entity(info: DomainInfo, class_to_pkg: dict[str, str], enum_names: set[str]) -> str:
    class_name = info.class_name
    table = info.mapping.get("table") or decapitalize(class_name)
    imports = {
        "jakarta.persistence.Column",
        "jakarta.persistence.Entity",
        "jakarta.persistence.GeneratedValue",
        "jakarta.persistence.GenerationType",
        "jakarta.persistence.Id",
        "jakarta.persistence.Table",
        "lombok.Getter",
        "lombok.Setter",
    }
    uses_date = False
    uses_bigdecimal = False
    uses_biginteger = False
    uses_enum = False
    uses_many_to_one = False
    uses_convert = False
    uses_temporal = False
    uses_version = info.mapping.get("version", True)
    uses_lob = False
    composite = info.mapping.get("composite") or []

    body_fields = []
    for f in info.fields:
        t = f.type_name
        entity_names = set(class_to_pkg)
        if t in info.local_enums or (t in enum_names and t not in entity_names):
            f.is_enum = True
        elif t not in PRIMITIVES and t != "byte[]":
            f.is_entity = True

        java_type = PRIMITIVES.get(t, t)
        if f.nullable and t in WRAPPER_FOR_PRIMITIVE:
            java_type = WRAPPER_FOR_PRIMITIVE[t]
        if java_type == "Date" or java_type == "java.util.Date":
            java_type = "Date"
            uses_date = True
            uses_temporal = True
            imports.add("java.util.Date")
        elif java_type == "java.sql.Timestamp":
            java_type = "Timestamp"
            imports.add("java.sql.Timestamp")
        elif java_type == "java.sql.Time":
            java_type = "Time"
            imports.add("java.sql.Time")
        if java_type.startswith("java.math.BigDecimal"):
            java_type = "BigDecimal"
            uses_bigdecimal = True
            imports.add("java.math.BigDecimal")
        if java_type.startswith("java.math.BigInteger"):
            java_type = "BigInteger"
            uses_biginteger = True
            imports.add("java.math.BigInteger")

        anns = []
        if f.name in composite:
            anns.append("    @Id")
        if f.is_entity:
            uses_many_to_one = True
            join = f"{f.name}_id"
            optional = "true" if f.nullable else "false"
            anns.append(f"    @ManyToOne(fetch = FetchType.LAZY)")
            anns.append(f'    @JoinColumn(name = "{join}", nullable = {optional})')
            imports.update({
                "jakarta.persistence.FetchType",
                "jakarta.persistence.JoinColumn",
                "jakarta.persistence.ManyToOne",
            })
            other_pkg = class_to_pkg.get(t)
            if other_pkg and other_pkg != BASE_PKG:
                imports.add(f"{other_pkg}.{t}")
        elif f.is_enum:
            uses_enum = True
            imports.update({
                "jakarta.persistence.EnumType",
                "jakarta.persistence.Enumerated",
            })
            imports.add(f"{ENUM_PKG}.{t}")
            col_args = [f'name = "{f.name}"']
            if f.nullable:
                col_args.append("nullable = true")
            else:
                col_args.append("nullable = false")
            if f.unique:
                col_args.append("unique = true")
            anns.append("    @Enumerated(EnumType.STRING)")
            anns.append(f'    @Column({", ".join(col_args)})')
        else:
            col_args = [f'name = "{f.name}"']
            if f.nullable:
                col_args.append("nullable = true")
            else:
                col_args.append("nullable = false")
            if f.unique:
                col_args.append("unique = true")
            if f.sql_type:
                col_args.append(f'columnDefinition = "{f.sql_type}"')
            if t in {"Date"}:
                anns.append("    @Temporal(TemporalType.TIMESTAMP)")
                imports.add("jakarta.persistence.Temporal")
                imports.add("jakarta.persistence.TemporalType")
            if f.yes_no:
                uses_convert = True
                anns.append("    @Convert(converter = YesNoConverter.class)")
                imports.add("jakarta.persistence.Convert")
                imports.add("org.hibernate.type.YesNoConverter")
                col_args.append("length = 1")
            if t == "byte[]":
                java_type = "byte[]"
                uses_lob = True
                anns.append("    @Lob")
                imports.add("jakarta.persistence.Lob")
            anns.append(f'    @Column({", ".join(col_args)})')

        default = java_default(f.default, t)
        decl = f"    private {java_type} {f.name}"
        if default:
            decl += f" = {default}"
        decl += ";"
        body_fields.append("\n".join(anns + [decl]))

    class_anns = ["@Entity", f'@Table(name = "{table}")', "@Getter", "@Setter"]
    implements = ""
    extra_classes = ""

    id_block = []
    if composite:
        imports.update({
            "jakarta.persistence.IdClass",
            "java.io.Serializable",
        })
        class_anns.insert(1, f"@IdClass({class_name}Id.class)")
        implements = " implements Serializable"
        extra_classes = generate_id_class(class_name, composite)
    else:
        id_block = [
            "    @Id",
            "    @GeneratedValue(strategy = GenerationType.IDENTITY)",
            "    private Long id;",
        ]
        if uses_version:
            imports.add("jakarta.persistence.Version")
            id_block += [
                "",
                "    @Version",
                "    private Long version;",
            ]

    import_lines = "\n".join(f"import {x};" for x in sorted(imports))
    pieces = []
    if id_block:
        pieces.append("\n".join(id_block))
    pieces.extend(body_fields)
    field_src = "\n\n".join(pieces)

    return (
        f"package {BASE_PKG};\n\n"
        f"{import_lines}\n\n"
        + "\n".join(class_anns) + "\n"
        f"public class {class_name}{implements} {{\n\n"
        f"{field_src}\n"
        f"}}\n"
        f"{extra_classes}"
    )


def generate_id_class(class_name: str, composite: list[str]) -> str:
    fields = "\n".join(f"    private Long {n};" for n in composite)
    eq_fields = " && ".join(f"Objects.equals({n}, that.{n})" for n in composite)
    hash_fields = ", ".join(composite)
    return f"""

@Getter
@Setter
class {class_name}Id implements Serializable {{
{fields}

    @Override
    public boolean equals(Object o) {{
        if (this == o) {{
            return true;
        }}
        if (!(o instanceof {class_name}Id that)) {{
            return false;
        }}
        return {eq_fields};
    }}

    @Override
    public int hashCode() {{
        return Objects.hash({hash_fields});
    }}
}}
"""


def main() -> None:
    if OUT_ROOT.exists():
        for p in OUT_ROOT.rglob("*.java"):
            p.unlink()

    domains: list[DomainInfo] = []
    all_enums: dict[str, list[str]] = {}

    for path in sorted(DOMAIN_ROOT.rglob("*.groovy")):
        info, enums = parse_domain(path)
        all_enums.update(enums)
        if info:
            domains.append(info)

    for root in ENUM_ROOTS:
        if not root.exists():
            continue
        for path in root.rglob("*.groovy"):
            parsed = parse_standalone_enum(path)
            if parsed:
                all_enums[parsed[0]] = parsed[1]

    enum_names = set(all_enums)
    class_to_pkg = {d.class_name: BASE_PKG for d in domains}

    OUT_ROOT.mkdir(parents=True, exist_ok=True)
    OUT_ROOT.joinpath("enums").mkdir(parents=True, exist_ok=True)

    for name, constants in sorted(all_enums.items()):
        write_enum(name, constants)

    for info in domains:
        src = generate_entity(info, class_to_pkg, enum_names)
        if info.mapping.get("composite"):
            src = src.replace("import java.io.Serializable;", "import java.io.Serializable;\nimport java.util.Objects;")
        (OUT_ROOT / f"{info.class_name}.java").write_text(src, encoding="utf-8")

    print(f"entities={len(domains)} enums={len(all_enums)}")
    print("entity names:", ", ".join(d.class_name for d in domains))
    print("enum names:", ", ".join(sorted(all_enums)))


if __name__ == "__main__":
    main()
