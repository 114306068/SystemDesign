"""所有資料表。依 DFD 資料儲存分檔：

D1 members.py   會員資料（users / performers / venues + 別名）
D2 events.py    演出資訊（events / event_performers / crawl_sources / crawl_runs）
D3 matching.py  場地檔期（venue_slots）
D4 matching.py  媒合紀錄（match_requests）
D5 follows.py   追蹤清單（follows / notifications）
D6 merch.py     商品與訂單（products / orders / order_items）
D7 billing.py   交易紀錄（transactions / commission_rules / promotions）
"""
from app.models.billing import CommissionRule, Promotion, Transaction
from app.models.events import CrawlRun, CrawlSource, Event, event_performers
from app.models.follows import Follow, Notification
from app.models.matching import MatchRequest, VenueSlot
from app.models.members import Performer, PerformerAlias, User, Venue, VenueAlias
from app.models.merch import Order, OrderItem, Product

__all__ = [
    "CommissionRule", "Promotion", "Transaction",
    "CrawlRun", "CrawlSource", "Event", "event_performers",
    "Follow", "Notification",
    "MatchRequest", "VenueSlot",
    "Performer", "PerformerAlias", "User", "Venue", "VenueAlias",
    "Order", "OrderItem", "Product",
]
