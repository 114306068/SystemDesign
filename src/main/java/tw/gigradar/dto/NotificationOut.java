package tw.gigradar.dto;

import java.time.LocalDateTime;
import tw.gigradar.model.Notification;

public record NotificationOut(Integer id, Integer eventId, String message, boolean isRead, LocalDateTime createdAt) {

    public static NotificationOut from(Notification n) {
        return new NotificationOut(n.getId(), n.getEvent().getId(), n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}
