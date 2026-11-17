"""4.0 處理周邊預購。

已完成：列出表演者的商品。
待實作：
  4.1 POST /api/performers/{id}/products   表演者上架商品
  4.2 POST /api/orders                     觀眾預購（檢查庫存、鎖定單價、產生 pickup_code 取貨憑證）
  4.3 GET  /api/events/{id}/pickup-list    演出當日取貨清單（給表演者）
      POST /api/orders/{id}/pickup         現場核銷取貨
"""
from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Product
from app.schemas import ProductOut

router = APIRouter(prefix="/api", tags=["4.0 周邊預購"])


@router.get("/performers/{performer_id}/products", response_model=list[ProductOut])
def list_products(performer_id: int, db: Session = Depends(get_db)):
    stmt = select(Product).where(Product.performer_id == performer_id, Product.is_active.is_(True))
    return db.scalars(stmt.order_by(Product.id)).all()
