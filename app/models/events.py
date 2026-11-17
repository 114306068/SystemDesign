"""D2 演出資訊，以及爬蟲來源 / 抓取紀錄。"""
from datetime import date, datetime, time

from sqlalchemy import Column, Enum, ForeignKey, Integer, String, Table, Text, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base
from app.models.enums import CrawlRunStatus, EventSource
from app.models.members import Performer, Venue

# 一場演出可以有多組表演者（共演）
event_performers = Table(
    "event_performers",
    Base.metadata,
    Column("event_id", Integer, ForeignKey("events.id", ondelete="CASCADE"), primary_key=True),
    Column("performer_id", Integer, ForeignKey("performers.id", ondelete="CASCADE"), primary_key=True),
)


class Event(Base):
    __tablename__ = "events"
    # 去重規則：日期＋場館＋開演時間 視為同一場
    __table_args__ = (
        UniqueConstraint("venue_id", "event_date", "start_time", name="uq_event_dedup"),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    title: Mapped[str] = mapped_column(String(255))
    venue_id: Mapped[int] = mapped_column(ForeignKey("venues.id"), index=True)
    event_date: Mapped[date] = mapped_column(index=True)
    start_time: Mapped[time | None]
    genre: Mapped[str | None] = mapped_column(String(50), index=True)
    description: Mapped[str | None] = mapped_column(Text)
    ticket_url: Mapped[str | None] = mapped_column(String(500))  # 購票導回原售票網站
    source_type: Mapped[EventSource] = mapped_column(Enum(EventSource))
    crawl_source_id: Mapped[int | None] = mapped_column(ForeignKey("crawl_sources.id"))
    source_url: Mapped[str | None] = mapped_column(String(500))  # 資料來源連結
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(server_default=func.now(), onupdate=func.now())

    venue: Mapped[Venue] = relationship()
    performers: Mapped[list[Performer]] = relationship(secondary=event_performers)


class CrawlSource(Base):
    """爬蟲來源（由平台管理者設定）。"""

    __tablename__ = "crawl_sources"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(100), unique=True)
    module: Mapped[str] = mapped_column(String(100))  # crawler/sources/ 底下的模組名
    base_url: Mapped[str | None] = mapped_column(String(500))
    expected_min_count: Mapped[int] = mapped_column(default=1)  # 低於此筆數視為異常
    is_active: Mapped[bool] = mapped_column(default=True)


class CrawlRun(Base):
    """每次抓取的紀錄，用來監控網站改版造成的失效。"""

    __tablename__ = "crawl_runs"

    id: Mapped[int] = mapped_column(primary_key=True)
    source_id: Mapped[int] = mapped_column(ForeignKey("crawl_sources.id"))
    started_at: Mapped[datetime] = mapped_column(server_default=func.now())
    finished_at: Mapped[datetime | None]
    fetched_count: Mapped[int] = mapped_column(default=0)
    inserted_count: Mapped[int] = mapped_column(default=0)
    merged_count: Mapped[int] = mapped_column(default=0)
    status: Mapped[CrawlRunStatus | None] = mapped_column(Enum(CrawlRunStatus))
    message: Mapped[str | None] = mapped_column(Text)
