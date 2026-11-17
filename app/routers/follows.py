"""2.0 查詢與追蹤演出 —— 2.2 設定追蹤、2.3 新演出通知。

已完成：查看通知。
待實作：
  2.2 POST /api/users/{id}/follows     新增追蹤（表演者 / 場館 / 風格），程式檢查重複
      DELETE /api/follows/{id}         取消追蹤
      POST /api/notifications/{id}/read
（登入機制做好後，{id} 改成從登入狀態取得目前使用者）
"""
from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Notification
from app.schemas import NotificationOut

router = APIRouter(prefix="/api/users", tags=["2.0 追蹤與通知"])


@router.get("/{user_id}/notifications", response_model=list[NotificationOut])
def list_notifications(user_id: int, unread_only: bool = False, db: Session = Depends(get_db)):
    stmt = select(Notification).where(Notification.user_id == user_id)
    if unread_only:
        stmt = stmt.where(Notification.is_read.is_(False))
    return db.scalars(stmt.order_by(Notification.created_at.desc())).all()
