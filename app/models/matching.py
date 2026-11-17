"""D3 場地檔期、D4 媒合紀錄。"""
from datetime import date, datetime, time
from decimal import Decimal

from sqlalchemy import Enum, ForeignKey, Numeric, Text, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base
from app.models.enums import MatchStatus, SlotStatus
from app.models.events import Event
from app.models.members import Performer, Venue


class VenueSlot(Base):
    __tablename__ = "venue_slots"
    __table_args__ = (UniqueConstraint("venue_id", "slot_date", "start_time", name="uq_slot"),)

    id: Mapped[int] = mapped_column(primary_key=True)
    venue_id: Mapped[int] = mapped_column(ForeignKey("venues.id"), index=True)
    slot_date: Mapped[date] = mapped_column(index=True)
    start_time: Mapped[time]
    end_time: Mapped[time]
    rent: Mapped[Decimal] = mapped_column(Numeric(10, 0))  # 原場租
    last_minute_rent: Mapped[Decimal | None] = mapped_column(Numeric(10, 0))  # 即期優惠價
    apply_deadline: Mapped[datetime | None]  # 時限：超過即不再接受申請
    allow_co_performance: Mapped[bool] = mapped_column(default=False)  # 共演招募
    max_performers: Mapped[int] = mapped_column(default=1)
    status: Mapped[SlotStatus] = mapped_column(Enum(SlotStatus), default=SlotStatus.open)
    event_id: Mapped[int | None] = mapped_column(ForeignKey("events.id"))  # 媒合成功後建立的演出
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())

    venue: Mapped[Venue] = relationship()
    event: Mapped[Event | None] = relationship()
    requests: Mapped[list["MatchRequest"]] = relationship(back_populates="slot")

    @property
    def effective_rent(self) -> Decimal:
        return self.last_minute_rent if self.last_minute_rent is not None else self.rent


class MatchRequest(Base):
    __tablename__ = "match_requests"
    __table_args__ = (UniqueConstraint("slot_id", "performer_id", name="uq_match_once"),)

    id: Mapped[int] = mapped_column(primary_key=True)
    slot_id: Mapped[int] = mapped_column(ForeignKey("venue_slots.id"), index=True)
    performer_id: Mapped[int] = mapped_column(ForeignKey("performers.id"), index=True)
    message: Mapped[str | None] = mapped_column(Text)
    status: Mapped[MatchStatus] = mapped_column(Enum(MatchStatus), default=MatchStatus.pending)
    final_rent: Mapped[Decimal | None] = mapped_column(Numeric(10, 0))  # 成交場租（共演時為分攤後金額）
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())
    decided_at: Mapped[datetime | None]

    slot: Mapped[VenueSlot] = relationship(back_populates="requests")
    performer: Mapped[Performer] = relationship()
