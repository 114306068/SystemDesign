"""2.3 發送新演出通知：找出追蹤此演出表演者 / 場館 / 風格的觀眾，建立通知。"""
from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from app.models import Event, Follow, Notification
from app.models.enums import FollowTarget


def notify_followers(db: Session, event: Event) -> int:
    performer_ids = [p.id for p in event.performers]
    conditions = [
        (Follow.target_type == FollowTarget.venue) & (Follow.venue_id == event.venue_id),
    ]
    if performer_ids:
        conditions.append(
            (Follow.target_type == FollowTarget.performer) & (Follow.performer_id.in_(performer_ids))
        )
    if event.genre:
        conditions.append((Follow.target_type == FollowTarget.genre) & (Follow.genre == event.genre))

    user_ids = set(db.scalars(select(Follow.user_id).where(or_(*conditions))))
    already = set(
        db.scalars(select(Notification.user_id).where(Notification.event_id == event.id))
    )
    names = "、".join(p.name for p in event.performers) or event.title
    created = 0
    for uid in sorted(user_ids - already):
        db.add(Notification(
            user_id=uid,
            event_id=event.id,
            message=f"{names} 有新演出：{event.event_date:%m/%d} @ {event.venue.name}",
        ))
        created += 1
    db.flush()
    # TODO(2.3)：寄 Email —— 之後加一個排程掃 emailed_at IS NULL 的通知寄出
    return created
