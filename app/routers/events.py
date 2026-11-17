"""2.0 查詢與追蹤演出 —— 2.1 查詢演出。"""
from datetime import date

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import or_, select
from sqlalchemy.orm import Session, selectinload

from app.database import get_db
from app.models import Event, Performer, PerformerAlias, Venue
from app.schemas import EventOut

router = APIRouter(prefix="/api/events", tags=["2.0 演出查詢"])


@router.get("", response_model=list[EventOut])
def search_events(
    date_from: date | None = Query(None, description="起始日期（預設今天）"),
    date_to: date | None = None,
    district: str | None = Query(None, description="地區，例：大安區"),
    venue_id: int | None = None,
    performer: str | None = Query(None, description="表演者名稱或別名（部分符合）"),
    genre: str | None = None,
    q: str | None = Query(None, description="關鍵字（演出名稱）"),
    limit: int = Query(50, le=200),
    db: Session = Depends(get_db),
):
    stmt = (
        select(Event)
        .join(Event.venue)
        .options(selectinload(Event.venue), selectinload(Event.performers))
        .where(Event.event_date >= (date_from or date.today()))
    )
    if date_to:
        stmt = stmt.where(Event.event_date <= date_to)
    if district:
        stmt = stmt.where(Venue.district == district)
    if venue_id:
        stmt = stmt.where(Event.venue_id == venue_id)
    if genre:
        stmt = stmt.where(Event.genre == genre)
    if q:
        stmt = stmt.where(Event.title.contains(q))
    if performer:
        # 名稱或別名任一符合
        matched = (
            select(Performer.id)
            .outerjoin(PerformerAlias)
            .where(or_(Performer.name.contains(performer), PerformerAlias.alias.contains(performer)))
        )
        stmt = stmt.where(Event.performers.any(Performer.id.in_(matched)))
    stmt = stmt.order_by(Event.event_date, Event.start_time).limit(limit)
    return db.scalars(stmt).unique().all()


@router.get("/{event_id}", response_model=EventOut)
def get_event(event_id: int, db: Session = Depends(get_db)):
    event = db.get(Event, event_id)
    if event is None:
        raise HTTPException(404, "找不到這場演出")
    return event
