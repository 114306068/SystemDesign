from datetime import date, time

from sqlalchemy import select

from app.models import Event, Notification
from crawler.base import RawEvent
from crawler.pipeline import normalize, parse_date, parse_time, upsert_events


def test_parse_date_formats():
    today = date(2026, 11, 17)
    assert parse_date("2026/12/05", today) == date(2026, 12, 5)
    assert parse_date("2026-12-05", today) == date(2026, 12, 5)
    assert parse_date("12月5日(六)", today) == date(2026, 12, 5)
    assert parse_date("1/10", today) == date(2027, 1, 10)  # 沒寫年份 → 往後推


def test_parse_time_formats():
    assert parse_time("19:30") == time(19, 30)
    assert parse_time("晚上7:30") == time(19, 30)
    assert parse_time("下午3點") == time(15, 0)
    assert parse_time(None) is None


def _raw(**kw):
    base = dict(title="測試演出", venue_name="北岸 LIVE HOUSE", date_text="2027/01/15",
                time_text="20:00", performers=["霧島樂隊"], genre="indie")
    return RawEvent(**(base | kw))


def test_duplicate_from_two_sources_is_merged(db):
    a = _raw(ticket_url=None)
    b = _raw(venue_name="北岸Live House", performers=["Kirishima Band"], ticket_url="https://t.example/1")
    result = upsert_events(db, None, [normalize(a), normalize(b)])
    assert (result.inserted, result.merged) == (1, 1)
    ev = db.scalars(select(Event).where(Event.event_date == date(2027, 1, 15))).one()
    assert ev.ticket_url == "https://t.example/1"          # 合併時補上缺的欄位
    assert [p.name for p in ev.performers] == ["霧島樂隊"]  # 別名對到同一團


def test_unknown_venue_is_skipped(db):
    result = upsert_events(db, None, [normalize(_raw(venue_name="不存在的場館"))])
    assert result.inserted == 0 and result.skipped


def test_new_event_notifies_followers(db):
    upsert_events(db, None, [normalize(_raw())])
    ev = db.scalars(select(Event).where(Event.event_date == date(2027, 1, 15))).one()
    notes = db.scalars(select(Notification).where(Notification.event_id == ev.id)).all()
    assert len(notes) == 1 and "霧島樂隊" in notes[0].message
