"""API 輸出格式（Pydantic）。"""
from datetime import date, datetime, time
from decimal import Decimal

from pydantic import BaseModel, ConfigDict


class ORM(BaseModel):
    model_config = ConfigDict(from_attributes=True)


class VenueOut(ORM):
    id: int
    name: str
    district: str
    address: str | None
    capacity: int | None
    official_url: str | None
    claim_status: str


class PerformerOut(ORM):
    id: int
    name: str
    genre: str | None
    social_url: str | None


class EventOut(ORM):
    id: int
    title: str
    event_date: date
    start_time: time | None
    genre: str | None
    ticket_url: str | None
    source_type: str
    source_url: str | None
    updated_at: datetime
    venue: VenueOut
    performers: list[PerformerOut]


class SlotOut(ORM):
    id: int
    slot_date: date
    start_time: time
    end_time: time
    rent: Decimal
    last_minute_rent: Decimal | None
    effective_rent: Decimal
    apply_deadline: datetime | None
    allow_co_performance: bool
    max_performers: int
    status: str
    venue: VenueOut


class MatchRequestOut(ORM):
    id: int
    slot_id: int
    status: str
    message: str | None
    final_rent: Decimal | None
    created_at: datetime
    performer: PerformerOut


class NotificationOut(ORM):
    id: int
    event_id: int
    message: str
    is_read: bool
    created_at: datetime


class ProductOut(ORM):
    id: int
    performer_id: int
    name: str
    description: str | None
    price: Decimal
    stock: int
    image_url: str | None


class CommissionRuleOut(ORM):
    id: int
    rule_type: str
    rate: Decimal
    effective_from: date
