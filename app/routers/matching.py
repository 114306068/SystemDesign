"""3.0 管理檔期與預訂。

已完成：3.2 查詢可用空檔、查看某空檔的申請。
待實作（雛型開發 I 階段）：
  3.1 POST /api/slots                     場地方上架空檔（需已認領場館）
  3.3 POST /api/slots/{id}/requests       表演者提出申請
  3.4 POST /api/requests/{id}/accept      場地方確認 → slot.status = booked
  3.5 （3.4 內呼叫）建立 Event(source_type=match)，並呼叫 notify_followers
"""
from datetime import date, datetime

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import or_, select
from sqlalchemy.orm import Session, selectinload

from app.database import get_db
from app.models import MatchRequest, Venue, VenueSlot
from app.models.enums import SlotStatus
from app.schemas import MatchRequestOut, SlotOut

router = APIRouter(prefix="/api/slots", tags=["3.0 場地媒合"])


@router.get("", response_model=list[SlotOut])
def list_open_slots(
    date_from: date | None = None,
    date_to: date | None = None,
    district: str | None = None,
    co_performance_only: bool = False,
    db: Session = Depends(get_db),
):
    """3.2 查詢可用空檔：只列 open、且尚未超過申請時限的檔期。"""
    stmt = (
        select(VenueSlot)
        .join(VenueSlot.venue)
        .options(selectinload(VenueSlot.venue))
        .where(
            VenueSlot.status == SlotStatus.open,
            VenueSlot.slot_date >= (date_from or date.today()),
            or_(VenueSlot.apply_deadline.is_(None), VenueSlot.apply_deadline >= datetime.now()),
        )
    )
    if date_to:
        stmt = stmt.where(VenueSlot.slot_date <= date_to)
    if district:
        stmt = stmt.where(Venue.district == district)
    if co_performance_only:
        stmt = stmt.where(VenueSlot.allow_co_performance.is_(True))
    return db.scalars(stmt.order_by(VenueSlot.slot_date, VenueSlot.start_time)).all()


@router.get("/{slot_id}/requests", response_model=list[MatchRequestOut])
def list_requests(slot_id: int, db: Session = Depends(get_db)):
    if db.get(VenueSlot, slot_id) is None:
        raise HTTPException(404, "找不到這個檔期")
    stmt = (
        select(MatchRequest)
        .options(selectinload(MatchRequest.performer))
        .where(MatchRequest.slot_id == slot_id)
        .order_by(MatchRequest.created_at)
    )
    return db.scalars(stmt).all()
