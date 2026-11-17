"""爬蟲流程：1.1 擷取 → 1.2 整理格式 → 1.3 比對並寫入 D2。

用法：python -m crawler.pipeline              # 跑所有啟用中的來源
      python -m crawler.pipeline sample_json  # 只跑一個來源
"""
import importlib
import re
import sys
from dataclasses import dataclass
from datetime import date, datetime, time

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.database import SessionLocal
from app.models import CrawlRun, CrawlSource, Event
from app.models.enums import CrawlRunStatus, EventSource
from app.services.dedup import find_duplicate_event, find_or_create_performer, find_venue
from app.services.notify import notify_followers
from crawler.base import RawEvent


# ---------- 1.2 整理資料格式 ----------

def parse_date(text: str, today: date | None = None) -> date:
    """支援 2026/12/05、2026-12-05、12月5日(六)、12/5。沒寫年份時取今天以後最近的那一天。"""
    today = today or date.today()
    m = re.search(r"(\d{4})\s*[/\-.年]\s*(\d{1,2})\s*[/\-.月]\s*(\d{1,2})", text)
    if m:
        return date(int(m[1]), int(m[2]), int(m[3]))
    m = re.search(r"(\d{1,2})\s*[/月]\s*(\d{1,2})", text)
    if m:
        d = date(today.year, int(m[1]), int(m[2]))
        return d if d >= today else date(today.year + 1, d.month, d.day)
    raise ValueError(f"無法解析日期：{text!r}")


def parse_time(text: str | None) -> time | None:
    """支援 19:30、7:30PM、晚上7:30、下午3點。"""
    if not text:
        return None
    m = re.search(r"(\d{1,2})\s*[:：點]\s*(\d{1,2})?", text)
    if not m:
        return None
    hour, minute = int(m[1]), int(m[2] or 0)
    if hour < 12 and re.search(r"(下午|晚上|晚間|pm|PM)", text):
        hour += 12
    return time(hour, minute)


@dataclass
class CleanEvent:
    raw: RawEvent
    event_date: date
    start_time: time | None


def normalize(raw: RawEvent, today: date | None = None) -> CleanEvent:
    return CleanEvent(raw=raw, event_date=parse_date(raw.date_text, today), start_time=parse_time(raw.time_text))


# ---------- 1.3 比對並寫入 ----------

@dataclass
class RunResult:
    fetched: int = 0
    inserted: int = 0
    merged: int = 0
    skipped: list[str] | None = None
    notified: int = 0


def upsert_events(db: Session, source: CrawlSource | None, cleaned: list[CleanEvent]) -> RunResult:
    result = RunResult(fetched=len(cleaned), skipped=[])
    for ce in cleaned:
        raw = ce.raw
        venue = find_venue(db, raw.venue_name)
        if venue is None:
            # 不自動建場館：交給管理者確認後新增場館或別名，避免同一場館被建成兩筆
            result.skipped.append(f"未知場館「{raw.venue_name}」：{raw.title}")
            continue
        performers = [find_or_create_performer(db, name, raw.genre) for name in raw.performers]

        existing = find_duplicate_event(db, venue.id, ce.event_date, ce.start_time)
        if existing:
            # 合併：補上缺的欄位、加入新出現的表演者
            existing.start_time = existing.start_time or ce.start_time
            existing.ticket_url = existing.ticket_url or raw.ticket_url
            existing.genre = existing.genre or raw.genre
            for p in performers:
                if p not in existing.performers:
                    existing.performers.append(p)
            result.merged += 1
            continue

        event = Event(
            title=raw.title,
            venue=venue,
            event_date=ce.event_date,
            start_time=ce.start_time,
            genre=raw.genre,
            ticket_url=raw.ticket_url,
            source_type=EventSource.crawler,
            crawl_source_id=source.id if source else None,
            source_url=raw.source_url,
            performers=performers,
        )
        db.add(event)
        db.flush()
        result.inserted += 1
        result.notified += notify_followers(db, event)
    return result


def run_source(db: Session, source: CrawlSource) -> CrawlRun:
    run = CrawlRun(source_id=source.id, started_at=datetime.now())
    db.add(run)
    try:
        module = importlib.import_module(f"crawler.sources.{source.module}")
        raws = module.SOURCE().fetch()                       # 1.1
        cleaned, bad = [], []
        for r in raws:                                        # 1.2
            try:
                cleaned.append(normalize(r))
            except ValueError as e:
                bad.append(str(e))
        result = upsert_events(db, source, cleaned)           # 1.3
        run.fetched_count = len(raws)
        run.inserted_count = result.inserted
        run.merged_count = result.merged
        problems = bad + (result.skipped or [])
        # 網站改版失效對策：抓到的筆數低於預期 → 標記異常
        run.status = CrawlRunStatus.anomaly if len(raws) < source.expected_min_count else CrawlRunStatus.success
        run.message = "\n".join(problems) or None
    except Exception as e:  # noqa: BLE001  任何錯誤都要留下紀錄
        db.rollback()
        db.add(run)
        run.status = CrawlRunStatus.failed
        run.message = repr(e)
    run.finished_at = datetime.now()
    db.commit()
    if run.status != CrawlRunStatus.success:
        # TODO：寄 Email 給維護者（提案書：異常時自動通知維護者）
        print(f"[警告] 來源 {source.name} 狀態 {run.status.value}：{run.message}")
    return run


def main(argv: list[str]) -> None:
    with SessionLocal() as db:
        stmt = select(CrawlSource).where(CrawlSource.is_active.is_(True))
        if argv:
            stmt = stmt.where(CrawlSource.module.in_(argv))
        for source in db.scalars(stmt).all():
            run = run_source(db, source)
            print(
                f"{source.name}: 抓到 {run.fetched_count}、新增 {run.inserted_count}、"
                f"合併 {run.merged_count}、狀態 {run.status.value}"
            )
            if run.message:
                print("  " + run.message.replace("\n", "\n  "))


if __name__ == "__main__":
    main(sys.argv[1:])
