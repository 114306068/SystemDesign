package tw.gigradar.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tw.gigradar.dto.FollowRequest;
import tw.gigradar.model.Follow;
import tw.gigradar.model.Notification;
import tw.gigradar.model.Performer;
import tw.gigradar.model.User;
import tw.gigradar.model.Venue;
import tw.gigradar.model.enums.FollowTarget;
import tw.gigradar.repository.FollowRepository;
import tw.gigradar.repository.NotificationRepository;
import tw.gigradar.repository.PerformerRepository;
import tw.gigradar.repository.UserRepository;
import tw.gigradar.repository.VenueRepository;

/** 2.2 設定追蹤、2.3 查看 / 標示已讀通知（D5）。都只能操作自己的資料。 */
@Service
@Transactional
public class FollowService {

    private final FollowRepository follows;
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final PerformerRepository performers;
    private final VenueRepository venues;

    public FollowService(FollowRepository follows, NotificationRepository notifications, UserRepository users,
                         PerformerRepository performers, VenueRepository venues) {
        this.follows = follows;
        this.notifications = notifications;
        this.users = users;
        this.performers = performers;
        this.venues = venues;
    }

    @Transactional(readOnly = true)
    public List<Follow> list(Integer userId) {
        return follows.findByUserIdOrderByCreatedAtDescIdDesc(userId);
    }

    public Follow follow(Integer userId, FollowRequest req) {
        User user = users.getReferenceById(userId);
        Follow follow = switch (req.targetType()) {
            case performer -> {
                Performer p = performers.findById(required(req.performerId(), "請選擇表演者"))
                        .orElseThrow(() -> notFound("找不到這個表演者"));
                if (follows.existsByUserIdAndTargetTypeAndPerformerId(userId, FollowTarget.performer, p.getId())) throw duplicate();
                yield Follow.performer(user, p);
            }
            case venue -> {
                Venue v = venues.findById(required(req.venueId(), "請選擇場館"))
                        .orElseThrow(() -> notFound("找不到這個場館"));
                if (follows.existsByUserIdAndTargetTypeAndVenueId(userId, FollowTarget.venue, v.getId())) throw duplicate();
                yield Follow.venue(user, v);
            }
            case genre -> {
                String genre = req.genre() == null ? "" : req.genre().strip();
                if (genre.isEmpty() || genre.length() > 50) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請輸入音樂風格（最多 50 字）");
                }
                if (follows.existsByUserIdAndTargetTypeAndGenre(userId, FollowTarget.genre, genre)) throw duplicate();
                yield Follow.genre(user, genre);
            }
        };
        return follows.save(follow);
    }

    public void unfollow(Integer userId, Integer followId) {
        follows.delete(follows.findByIdAndUserId(followId, userId).orElseThrow(() -> notFound("找不到這筆追蹤")));
    }

    @Transactional(readOnly = true)
    public List<Notification> notifications(Integer userId, boolean unreadOnly) {
        return unreadOnly
                ? notifications.findByUserIdAndReadFalseOrderByCreatedAtDescIdDesc(userId)
                : notifications.findByUserIdOrderByCreatedAtDescIdDesc(userId);
    }

    public Notification markRead(Integer userId, Integer notificationId) {
        Notification n = notifications.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> notFound("找不到這則通知"));
        n.setRead(true);
        return n;
    }

    private static Integer required(Integer id, String message) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        return id;
    }

    private static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private static ResponseStatusException duplicate() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "已經追蹤過了");
    }
}
