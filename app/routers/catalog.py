"""場館與表演者的基本資料（D1），給查詢表單的下拉選單用。"""
from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Event, Performer, Venue
from app.schemas import PerformerOut, VenueOut

router = APIRouter(prefix="/api", tags=["基本資料"])


@router.get("/venues", response_model=list[VenueOut])
def list_venues(district: str | None = None, db: Session = Depends(get_db)):
    stmt = select(Venue).order_by(Venue.district, Venue.name)
    if district:
        stmt = stmt.where(Venue.district == district)
    return db.scalars(stmt).all()


@router.get("/districts", response_model=list[str])
def list_districts(db: Session = Depends(get_db)):
    return db.scalars(select(Venue.district).distinct().order_by(Venue.district)).all()


@router.get("/genres", response_model=list[str])
def list_genres(db: Session = Depends(get_db)):
    return db.scalars(
        select(Event.genre).where(Event.genre.is_not(None)).distinct().order_by(Event.genre)
    ).all()


@router.get("/performers", response_model=list[PerformerOut])
def list_performers(db: Session = Depends(get_db)):
    return db.scalars(select(Performer).order_by(Performer.name)).all()
