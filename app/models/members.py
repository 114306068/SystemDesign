"""D1 會員資料：三種角色帳號、表演者、場館與場館認領狀態。"""
from datetime import datetime

from sqlalchemy import Enum, ForeignKey, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base
from app.models.enums import ClaimStatus, UserRole


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(primary_key=True)
    email: Mapped[str] = mapped_column(String(255), unique=True)
    password_hash: Mapped[str] = mapped_column(String(255))
    display_name: Mapped[str] = mapped_column(String(100))
    role: Mapped[UserRole] = mapped_column(Enum(UserRole), default=UserRole.audience)
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())


class Performer(Base):
    """表演者（樂團）。爬蟲建立時 user_id 為空，表演者註冊後可認領。"""

    __tablename__ = "performers"

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int | None] = mapped_column(ForeignKey("users.id"), unique=True)
    name: Mapped[str] = mapped_column(String(150), unique=True)
    genre: Mapped[str | None] = mapped_column(String(50), index=True)
    bio: Mapped[str | None] = mapped_column(Text)
    social_url: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())

    aliases: Mapped[list["PerformerAlias"]] = relationship(
        back_populates="performer", cascade="all, delete-orphan"
    )


class PerformerAlias(Base):
    """樂團別名表：中英文、縮寫都對應到同一個樂團（爬蟲 1.3 比對用）。"""

    __tablename__ = "performer_aliases"

    id: Mapped[int] = mapped_column(primary_key=True)
    performer_id: Mapped[int] = mapped_column(ForeignKey("performers.id", ondelete="CASCADE"))
    alias: Mapped[str] = mapped_column(String(150), unique=True)

    performer: Mapped[Performer] = relationship(back_populates="aliases")


class Venue(Base):
    """場館。階段一由爬蟲建立（unclaimed），階段二由場地方認領後自行管理檔期。"""

    __tablename__ = "venues"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(150), unique=True)
    district: Mapped[str] = mapped_column(String(20), index=True)  # 例：大安區
    address: Mapped[str | None] = mapped_column(String(255))
    capacity: Mapped[int | None]
    official_url: Mapped[str | None] = mapped_column(String(500))
    owner_user_id: Mapped[int | None] = mapped_column(ForeignKey("users.id"))
    claim_status: Mapped[ClaimStatus] = mapped_column(
        Enum(ClaimStatus), default=ClaimStatus.unclaimed
    )
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())

    aliases: Mapped[list["VenueAlias"]] = relationship(
        back_populates="venue", cascade="all, delete-orphan"
    )


class VenueAlias(Base):
    """場館名稱對應（1.2 整理資料格式：各來源對同一場館的寫法不同）。"""

    __tablename__ = "venue_aliases"

    id: Mapped[int] = mapped_column(primary_key=True)
    venue_id: Mapped[int] = mapped_column(ForeignKey("venues.id", ondelete="CASCADE"))
    alias: Mapped[str] = mapped_column(String(150), unique=True)

    venue: Mapped[Venue] = relationship(back_populates="aliases")
