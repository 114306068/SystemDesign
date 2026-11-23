package tw.gigradar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tw.gigradar.model.enums.FollowTarget;

/**
 * D5 觀眾追蹤：表演者、場館或音樂風格，三選一。
 * 重複追蹤由程式檢查（MySQL 的 UNIQUE 不會擋含 NULL 的重複列）。
 */
@Entity
@Table(name = "follows")
@Check(name = "ck_follow_target", constraints =
        "(target_type = 'performer' AND performer_id IS NOT NULL)"
        + " OR (target_type = 'venue' AND venue_id IS NOT NULL)"
        + " OR (target_type = 'genre' AND genre IS NOT NULL)")
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowTarget targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Performer performer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Venue venue;

    @Column(length = 50)
    private String genre;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Follow() {}

    public static Follow performer(User user, Performer performer) {
        Follow f = new Follow();
        f.user = user;
        f.targetType = FollowTarget.performer;
        f.performer = performer;
        return f;
    }

    public static Follow venue(User user, Venue venue) {
        Follow f = new Follow();
        f.user = user;
        f.targetType = FollowTarget.venue;
        f.venue = venue;
        return f;
    }

    public static Follow genre(User user, String genre) {
        Follow f = new Follow();
        f.user = user;
        f.targetType = FollowTarget.genre;
        f.genre = genre;
        return f;
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public FollowTarget getTargetType() { return targetType; }
    public Performer getPerformer() { return performer; }
    public Venue getVenue() { return venue; }
    public String getGenre() { return genre; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
