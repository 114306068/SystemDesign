"""5.0 結算抽成與推廣。

已完成：查詢目前生效的抽成規則。
待實作：
  5.1 calc_commission()：媒合成交 / 訂單付款時，用當下規則算抽成並寫入 transactions
  5.2 POST /api/promotions                 表演者購買付費曝光（NT$500）
  5.3 POST /api/payments/{txn_id}/mock-pay 模擬金流回傳付款結果
  5.4 GET  /api/admin/reports/monthly      營運報表
"""
from datetime import date

from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import CommissionRule
from app.models.enums import CommissionRuleType
from app.schemas import CommissionRuleOut

router = APIRouter(prefix="/api/admin", tags=["5.0 帳務與推廣"])


def current_rule(db: Session, rule_type: CommissionRuleType, on: date | None = None) -> CommissionRule | None:
    stmt = (
        select(CommissionRule)
        .where(CommissionRule.rule_type == rule_type, CommissionRule.effective_from <= (on or date.today()))
        .order_by(CommissionRule.effective_from.desc(), CommissionRule.id.desc())
        .limit(1)
    )
    return db.scalars(stmt).first()


@router.get("/commission-rules/current", response_model=list[CommissionRuleOut])
def current_rules(db: Session = Depends(get_db)):
    return [r for t in CommissionRuleType if (r := current_rule(db, t))]
