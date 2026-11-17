import enum


class UserRole(str, enum.Enum):
    audience = "audience"      # 觀眾
    performer = "performer"    # 表演者
    venue = "venue"            # 場地方
    admin = "admin"            # 平台管理者


class ClaimStatus(str, enum.Enum):
    unclaimed = "unclaimed"    # 爬蟲建立、尚未認領
    pending = "pending"        # 已申請認領，待管理者審核
    approved = "approved"
    rejected = "rejected"


class EventSource(str, enum.Enum):
    crawler = "crawler"        # 1.0 爬蟲蒐集
    performer = "performer"    # 表演者自行上架
    match = "match"            # 3.5 媒合成功自動建立


class SlotStatus(str, enum.Enum):
    open = "open"              # 空檔，可申請
    booked = "booked"          # 已媒合成功
    closed = "closed"          # 場地方下架 / 過期


class MatchStatus(str, enum.Enum):
    pending = "pending"
    accepted = "accepted"
    rejected = "rejected"
    cancelled = "cancelled"


class FollowTarget(str, enum.Enum):
    performer = "performer"
    venue = "venue"
    genre = "genre"


class OrderStatus(str, enum.Enum):
    pending_payment = "pending_payment"
    paid = "paid"
    picked_up = "picked_up"
    cancelled = "cancelled"


class Fulfillment(str, enum.Enum):
    pickup = "pickup"          # 演出當天現場取貨（範疇內）
    shipping = "shipping"      # 寄送（宅配流程在範疇外，只保留欄位）


class TxnType(str, enum.Enum):
    match_commission = "match_commission"    # 媒合成功抽成
    merch_commission = "merch_commission"    # 周邊販售抽成
    promotion = "promotion"                  # 付費曝光


class PaymentStatus(str, enum.Enum):
    pending = "pending"
    paid = "paid"
    failed = "failed"


class CommissionRuleType(str, enum.Enum):
    match = "match"
    merch = "merch"


class CrawlRunStatus(str, enum.Enum):
    success = "success"
    anomaly = "anomaly"        # 抓到的筆數異常偏低 → 通知維護者
    failed = "failed"
