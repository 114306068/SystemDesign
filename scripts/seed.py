"""放入示範資料：台北 7 間合作場館（虛構名稱）、表演者、演出、空檔、商品、抽成規則。

用法：python -m scripts.seed   （會先清空再放，請只在開發環境使用）
日期都相對於今天產生，任何時候跑都看得到「近期演出」。
"""
from datetime import date, datetime, time, timedelta
from decimal import Decimal

from sqlalchemy.orm import Session

from app.database import Base, SessionLocal, engine
from app.models import (
    CommissionRule, CrawlSource, Event, Follow, Performer, PerformerAlias, Product, User, Venue,
    VenueAlias, VenueSlot,
)
from app.models.enums import ClaimStatus, CommissionRuleType, EventSource, FollowTarget, UserRole
from app.services.security import hash_password

VENUES = [
    # name, district, capacity, aliases
    ("北岸 Live House", "中正區", 400, ["北岸LIVE", "North Shore Live House"]),
    ("公館地下室", "中正區", 120, ["Gongguan Basement"]),
    ("大稻埕倉庫", "大同區", 250, ["Dadaocheng Warehouse"]),
    ("信義聲響廳", "信義區", 600, []),
    ("師大巷弄小劇場", "大安區", 80, []),
    ("松菸邊角", "信義區", 200, []),
    ("西門回聲", "萬華區", 300, ["Ximen Echo"]),
]

PERFORMERS = [
    # name, genre, aliases
    ("霧島樂隊", "indie", ["Kirishima Band", "霧島"]),
    ("藍調三重奏", "jazz", ["Blue Trio"]),
    ("午夜電台", "rock", ["Midnight Radio"]),
    ("小島民謠社", "folk", []),
    ("合成器少年", "electronic", ["Synth Boy"]),
]


def seed(db: Session) -> None:
    today = date.today()

    users = {
        "audience": User(email="audience@demo.tw", display_name="示範觀眾", role=UserRole.audience,
                         password_hash=hash_password("demo1234")),
        "performer": User(email="band@demo.tw", display_name="霧島樂隊（帳號）", role=UserRole.performer,
                          password_hash=hash_password("demo1234")),
        "venue": User(email="venue@demo.tw", display_name="北岸 Live House（帳號）", role=UserRole.venue,
                      password_hash=hash_password("demo1234")),
        "admin": User(email="admin@demo.tw", display_name="平台管理者", role=UserRole.admin,
                      password_hash=hash_password("demo1234")),
    }
    db.add_all(users.values())
    db.flush()

    venues = []
    for name, district, cap, aliases in VENUES:
        v = Venue(name=name, district=district, capacity=cap, address=f"台北市{district}（示範地址）",
                  aliases=[VenueAlias(alias=a) for a in aliases])
        venues.append(v)
    venues[0].owner_user_id = users["venue"].id
    venues[0].claim_status = ClaimStatus.approved  # 階段二：已認領
    db.add_all(venues)

    performers = [
        Performer(name=n, genre=g, aliases=[PerformerAlias(alias=a) for a in al]) for n, g, al in PERFORMERS
    ]
    performers[0].user_id = users["performer"].id
    db.add_all(performers)
    db.flush()

    # 近期演出（表演者自行上架），第 6 場為兩團共演
    plan = [(3, 0, 0, "19:30"), (5, 1, 1, "20:00"), (8, 2, 2, "19:00"), (10, 3, 3, "15:00"),
            (14, 4, 4, "21:00"), (17, 6, 0, "19:30"), (21, 2, 1, "20:00")]
    for i, (days, vi, pi, t) in enumerate(plan):
        p = performers[pi]
        ev = Event(
            title=f"{p.name} 現場演出" if i != 5 else "冬夜共演之夜",
            venue=venues[vi], event_date=today + timedelta(days=days),
            start_time=time.fromisoformat(t), genre=p.genre, source_type=EventSource.performer,
            ticket_url=f"https://tickets.example.com/demo/{i + 1}",
            performers=[p] if i != 5 else [performers[0], performers[2]],
        )
        db.add(ev)

    # 場地空檔：即期（一週內、有優惠價、有申請時限）與一般空檔
    for vi, days, rent, lm, co in [(0, 4, 30000, 18000, False), (1, 6, 12000, 8000, True),
                                   (3, 9, 45000, None, False), (5, 12, 20000, 15000, True)]:
        slot_date = today + timedelta(days=days)
        db.add(VenueSlot(
            venue=venues[vi], slot_date=slot_date, start_time=time(19, 0), end_time=time(22, 0),
            rent=Decimal(rent), last_minute_rent=Decimal(lm) if lm else None,
            apply_deadline=datetime.combine(slot_date - timedelta(days=2), time(23, 59)),
            allow_co_performance=co, max_performers=2 if co else 1,
        ))

    db.add_all([
        Product(performer_id=performers[0].id, name="霧島 巡演 T-shirt", price=Decimal(650), stock=40),
        Product(performer_id=performers[0].id, name="霧島 首張 EP（CD）", price=Decimal(400), stock=60),
        Product(performer_id=performers[1].id, name="藍調三重奏 帆布袋", price=Decimal(350), stock=25),
    ])

    db.add_all([
        CommissionRule(rule_type=CommissionRuleType.match, rate=Decimal("0.10"),
                       effective_from=today - timedelta(days=30), created_by=users["admin"].id),
        CommissionRule(rule_type=CommissionRuleType.merch, rate=Decimal("0.10"),
                       effective_from=today - timedelta(days=30), created_by=users["admin"].id),
    ])

    # 示範觀眾追蹤霧島樂隊與 jazz 風格（跑爬蟲時會收到通知）
    db.add_all([
        Follow(user_id=users["audience"].id, target_type=FollowTarget.performer, performer_id=performers[0].id),
        Follow(user_id=users["audience"].id, target_type=FollowTarget.genre, genre="jazz"),
    ])

    db.add(CrawlSource(name="範例 JSON 來源", module="sample_json", expected_min_count=3))
    db.commit()


def main() -> None:
    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)
    with SessionLocal() as db:
        seed(db)
    print("示範資料已建立。帳號密碼皆為 demo1234：audience@demo.tw / band@demo.tw / venue@demo.tw / admin@demo.tw")


if __name__ == "__main__":
    main()
