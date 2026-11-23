package tw.gigradar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** D5 新演出通知（2.3）。同一場演出對同一位使用者只通知一次。 */
@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "uq_notify_once", columnNames = {"user_id", "event_id"}))
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    private LocalDateTime emailedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Notification() {}

    public Notification(User user, Event event, String message) {
        this.user = user;
        this.event = event;
        this.message = message;
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getEmailedAt() { return emailedAt; }
    public void setEmailedAt(LocalDateTime emailedAt) { this.emailedAt = emailedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
