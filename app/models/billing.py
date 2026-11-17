"""D7 交易紀錄，以及抽成規則、付費曝光。"""
from datetime import date, datetime
from decimal import Decimal

from sqlalchemy import Enum, ForeignKey, Numeric, String, func
from sqlalchemy.orm import Mapped, mapped_column

from app.database import Base
from app.models.enums import CommissionRuleType, PaymentStatus, TxnType


class CommissionRule(Base):
    """平台管理者設定的抽成比例；以 effective_from 最新且已生效的一筆為準。"""

    __tablename__ = "commission_rules"

    id: Mapped[int] = mapped_column(primary_key=True)
    rule_type: Mapped[CommissionRuleType] = mapped_column(Enum(CommissionRuleType))
    rate: Mapped[Decimal] = mapped_column(Numeric(5, 4))  # 0.1000 = 10%
    effective_from: Mapped[date]
    created_by: Mapped[int | None] = mapped_column(ForeignKey("users.id"))
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())


class Promotion(Base):
    """付費曝光：表演者購買首頁 / 搜尋結果置頂。"""

    __tablename__ = "promotions"

    id: Mapped[int] = mapped_column(primary_key=True)
    performer_id: Mapped[int] = mapped_column(ForeignKey("performers.id"), index=True)
    event_id: Mapped[int | None] = mapped_column(ForeignKey("events.id"))
    fee: Mapped[Decimal] = mapped_column(Numeric(10, 0), default=500)
    start_date: Mapped[date]
    end_date: Mapped[date]
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())


class Transaction(Base):
    __tablename__ = "transactions"

    id: Mapped[int] = mapped_column(primary_key=True)
    txn_type: Mapped[TxnType] = mapped_column(Enum(TxnType), index=True)
    match_request_id: Mapped[int | None] = mapped_column(ForeignKey("match_requests.id"))
    order_id: Mapped[int | None] = mapped_column(ForeignKey("orders.id"))
    promotion_id: Mapped[int | None] = mapped_column(ForeignKey("promotions.id"))
    gross_amount: Mapped[Decimal] = mapped_column(Numeric(10, 0))       # 場租 / 訂單金額 / 曝光費
    commission_rate: Mapped[Decimal | None] = mapped_column(Numeric(5, 4))
    commission_amount: Mapped[Decimal] = mapped_column(Numeric(10, 0))  # 平台收入
    payment_status: Mapped[PaymentStatus] = mapped_column(Enum(PaymentStatus), default=PaymentStatus.pending)
    payment_ref: Mapped[str | None] = mapped_column(String(100))  # 模擬金流回傳編號
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())
    paid_at: Mapped[datetime | None]
