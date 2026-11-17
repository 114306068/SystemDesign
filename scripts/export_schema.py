"""由 SQLAlchemy models 產生 db/schema.sql（MySQL 語法），讓 models 與 SQL 永遠一致。

用法：python -m scripts.export_schema
"""
from pathlib import Path

from sqlalchemy.dialects import mysql
from sqlalchemy.schema import CreateIndex, CreateTable

import app.models  # noqa: F401  註冊所有資料表
from app.database import Base

OUT = Path(__file__).resolve().parent.parent / "db" / "schema.sql"


def main() -> None:
    dialect = mysql.dialect()
    parts = [
        "-- Gig Radar 演出雷達 資料庫結構（由 scripts/export_schema.py 自動產生，請勿手改）",
        "CREATE DATABASE IF NOT EXISTS gig_radar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
        "USE gig_radar;",
        "",
    ]
    for table in Base.metadata.sorted_tables:
        ddl = str(CreateTable(table).compile(dialect=dialect)).strip()
        parts.append(f"{ddl}\nENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n")
        for index in sorted(table.indexes, key=lambda i: i.name):
            parts.append(str(CreateIndex(index).compile(dialect=dialect)).strip() + ";\n")
    OUT.write_text("\n".join(parts), encoding="utf-8")
    print(f"wrote {OUT} ({len(Base.metadata.sorted_tables)} tables)")


if __name__ == "__main__":
    main()
