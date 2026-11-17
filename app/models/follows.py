"""D5 追蹤清單，以及新演出通知。"""
from datetime import datetime

from sqlalchemy import CheckConstraint, Enum, ForeignKey, String, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column

from app.database import Base
from app.models.enums import FollowTarget


class Follow(Base):
    """觀眾追蹤：表演者、場館或音樂風格，三選一。"""

    __tablename__ = "follows"
    # 註：重複追蹤由程式檢查（MySQL 的 UNIQUE 不會擋含 NULL 的重複列）
    __table_args__ = (
        CheckConstraint(
            "(target_type = 'performer' AND performer_id IS NOT NULL)"
            " OR (target_type = 'venue' AND venue_id IS NOT NULL)"
            " OR (target_type = 'genre' AND genre IS NOT NULL)",
            name="ck_follow_target",
        ),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    target_type: Mapped[FollowTarget] = mapped_column(Enum(FollowTarget))
    performer_id: Mapped[int | None] = mapped_column(ForeignKey("performers.id", ondelete="CASCADE"))
    venue_id: Mapped[int | None] = mapped_column(ForeignKey("venues.id", ondelete="CASCADE"))
    genre: Mapped[str | None] = mapped_column(String(50))
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())


class Notification(Base):
    """2.3 發送新演出通知。"""

    __tablename__ = "notifications"
    __table_args__ = (UniqueConstraint("user_id", "event_id", name="uq_notify_once"),)

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    event_id: Mapped[int] = mapped_column(ForeignKey("events.id", ondelete="CASCADE"))
    message: Mapped[str] = mapped_column(String(255))
    is_read: Mapped[bool] = mapped_column(default=False)
    emailed_at: Mapped[datetime | None]
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())
