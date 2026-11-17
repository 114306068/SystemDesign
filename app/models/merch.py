"""D6 商品與訂單。"""
from datetime import datetime
from decimal import Decimal

from sqlalchemy import Enum, ForeignKey, Numeric, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base
from app.models.enums import Fulfillment, OrderStatus


class Product(Base):
    __tablename__ = "products"

    id: Mapped[int] = mapped_column(primary_key=True)
    performer_id: Mapped[int] = mapped_column(ForeignKey("performers.id"), index=True)
    name: Mapped[str] = mapped_column(String(150))
    description: Mapped[str | None] = mapped_column(Text)
    price: Mapped[Decimal] = mapped_column(Numeric(10, 0))
    stock: Mapped[int] = mapped_column(default=0)
    image_url: Mapped[str | None] = mapped_column(String(500))
    is_active: Mapped[bool] = mapped_column(default=True)
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())


class Order(Base):
    __tablename__ = "orders"

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), index=True)
    performer_id: Mapped[int] = mapped_column(ForeignKey("performers.id"), index=True)
    event_id: Mapped[int | None] = mapped_column(ForeignKey("events.id"))  # 取貨場次
    fulfillment: Mapped[Fulfillment] = mapped_column(Enum(Fulfillment), default=Fulfillment.pickup)
    status: Mapped[OrderStatus] = mapped_column(Enum(OrderStatus), default=OrderStatus.pending_payment)
    total_amount: Mapped[Decimal] = mapped_column(Numeric(10, 0))
    pickup_code: Mapped[str | None] = mapped_column(String(20), unique=True)  # 取貨憑證
    created_at: Mapped[datetime] = mapped_column(server_default=func.now())
    picked_up_at: Mapped[datetime | None]

    items: Mapped[list["OrderItem"]] = relationship(back_populates="order", cascade="all, delete-orphan")


class OrderItem(Base):
    __tablename__ = "order_items"

    id: Mapped[int] = mapped_column(primary_key=True)
    order_id: Mapped[int] = mapped_column(ForeignKey("orders.id", ondelete="CASCADE"), index=True)
    product_id: Mapped[int] = mapped_column(ForeignKey("products.id"))
    quantity: Mapped[int]
    unit_price: Mapped[Decimal] = mapped_column(Numeric(10, 0))  # 下單當下價格

    order: Mapped[Order] = relationship(back_populates="items")
    product: Mapped[Product] = relationship()
