package tw.gigradar.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.gigradar.model.Event;
import tw.gigradar.model.Follow;
import tw.gigradar.model.Notification;
import tw.gigradar.model.Performer;
import tw.gigradar.model.enums.FollowTarget;
import tw.gigradar.repository.FollowRepository;
import tw.gigradar.repository.NotificationRepository;
import tw.gigradar.repository.UserRepository;

/** 2.3 發送新演出通知：找出追蹤此演出表演者 / 場館 / 風格的觀眾，建立通知。 */
@Service
@Transactional
public class NotifyService {

    private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MM/dd");

    private final FollowRepository follows;
    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotifyService(FollowRepository follows, NotificationRepository notifications, UserRepository users) {
        this.follows = follows;
        this.notifications = notifications;
        this.users = users;
    }

    /** @return 新建立的通知數 */
    public int notifyFollowers(Event event) {
        List<Integer> performerIds = event.getPerformers().stream().map(Performer::getId).toList();

        Set<Integer> userIds = new TreeSet<>(); // TreeSet：依 user id 排序，結果穩定
        addUsers(userIds, follows.findByTargetTypeAndVenueId(FollowTarget.venue, event.getVenue().getId()));
        if (!performerIds.isEmpty()) {
            addUsers(userIds, follows.findByTargetTypeAndPerformerIdIn(FollowTarget.performer, performerIds));
        }
        if (event.getGenre() != null) {
            addUsers(userIds, follows.findByTargetTypeAndGenre(FollowTarget.genre, event.getGenre()));
        }

        // 同一場演出不重複通知同一個人
        notifications.findByEventId(event.getId()).forEach(n -> userIds.remove(n.getUser().getId()));

        String names = event.getPerformers().isEmpty()
                ? event.getTitle()
                : event.getPerformers().stream().map(Performer::getName).collect(Collectors.joining("、"));
        String message = names + " 有新演出：" + event.getEventDate().format(MONTH_DAY) + " @ " + event.getVenue().getName();

        for (Integer uid : userIds) {
            notifications.save(new Notification(users.getReferenceById(uid), event, message));
        }
        notifications.flush();
        // TODO(2.3)：寄 Email —— 之後加一個排程掃 emailed_at IS NULL 的通知寄出
        return userIds.size();
    }

    private static void addUsers(Set<Integer> userIds, List<Follow> matched) {
        matched.forEach(f -> userIds.add(f.getUser().getId()));
    }
}
