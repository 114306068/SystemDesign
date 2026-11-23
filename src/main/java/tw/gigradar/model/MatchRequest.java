package tw.gigradar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import tw.gigradar.model.enums.MatchStatus;

/** D4 媒合紀錄：表演者對某個空檔提出的申請。同一團對同一空檔只能申請一次。 */
@Entity
@Table(
        name = "match_requests",
        uniqueConstraints = @UniqueConstraint(name = "uq_match_once", columnNames = {"slot_id", "performer_id"}))
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private VenueSlot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performer_id", nullable = false)
    private Performer performer;

    @Lob
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.pending;

    /** 成交場租（共演時為分攤後金額） */
    @Column(precision = 10, scale = 0)
    private BigDecimal finalRent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime decidedAt;

    protected MatchRequest() {}

    public MatchRequest(VenueSlot slot, Performer performer, String message) {
        this.slot = slot;
        this.performer = performer;
        this.message = message;
    }

    public Integer getId() { return id; }
    public VenueSlot getSlot() { return slot; }
    public Performer getPerformer() { return performer; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public BigDecimal getFinalRent() { return finalRent; }
    public void setFinalRent(BigDecimal finalRent) { this.finalRent = finalRent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}
