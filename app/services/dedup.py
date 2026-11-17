"""1.3 比對並寫入演出：去重與名稱比對。爬蟲與表演者上架都共用這裡。"""
import re
import unicodedata
from datetime import date, time

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models import Event, Performer, PerformerAlias, Venue, VenueAlias


def normalize_name(name: str) -> str:
    """全形轉半形、去空白與常見標點、英文轉小寫。
    大小寫、全半形、空白的差異在這裡處理；中英文名稱對應（例如中文團名 vs 英文團名）靠別名表。"""
    s = unicodedata.normalize("NFKC", name).lower()
    return re.sub(r"[\s\-_.·・'’\"()（）\[\]]+", "", s)


def find_performer(db: Session, raw_name: str) -> Performer | None:
    key = normalize_name(raw_name)
    for p in db.scalars(select(Performer)):
        if normalize_name(p.name) == key:
            return p
    # 雛型資料量小，直接全表比對；資料量變大時改成存 normalized 欄位並加索引
    for a in db.scalars(select(PerformerAlias)):
        if normalize_name(a.alias) == key:
            return a.performer
    return None


def find_or_create_performer(db: Session, raw_name: str, genre: str | None = None) -> Performer:
    performer = find_performer(db, raw_name)
    if performer is None:
        performer = Performer(name=raw_name.strip(), genre=genre)
        db.add(performer)
        db.flush()
    return performer


def find_venue(db: Session, raw_name: str) -> Venue | None:
    key = normalize_name(raw_name)
    for v in db.scalars(select(Venue)):
        if normalize_name(v.name) == key:
            return v
    for a in db.scalars(select(VenueAlias)):
        if normalize_name(a.alias) == key:
            return a.venue
    return None


def find_duplicate_event(db: Session, venue_id: int, event_date: date, start_time: time | None) -> Event | None:
    """日期＋場館＋開演時間 相同 → 同一場。沒有開演時間時，同場館同日期只有一場才視為同一場。"""
    stmt = select(Event).where(Event.venue_id == venue_id, Event.event_date == event_date)
    candidates = db.scalars(stmt).all()
    if start_time is not None:
        exact = [e for e in candidates if e.start_time == start_time]
        if exact:
            return exact[0]
        untimed = [e for e in candidates if e.start_time is None]
        return untimed[0] if len(untimed) == 1 else None
    return candidates[0] if len(candidates) == 1 else None
