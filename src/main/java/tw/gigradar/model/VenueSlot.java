package tw.gigradar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import tw.gigradar.model.enums.SlotStatus;

/** D3 場地檔期（空檔）。 */
@Entity
@Table(
        name = "venue_slots",
        uniqueConstraints = @UniqueConstraint(name = "uq_slot", columnNames = {"venue_id", "slot_date", "start_time"}),
        indexes = @Index(name = "ix_venue_slots_slot_date", columnList = "slot_date"))
public class VenueSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private LocalDate slotDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    /** 原場租 */
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal rent;

    /** 即期優惠價 */
    @Column(precision = 10, scale = 0)
    private BigDecimal lastMinuteRent;

    /** 時限：超過即不再接受申請 */
    private LocalDateTime applyDeadline;

    /** 共演招募 */
    @Column(nullable = false)
    private boolean allowCoPerformance = false;

    @Column(nullable = false)
    private int maxPerformers = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.open;

    /** 媒合成功後建立的演出 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "slot")
    private List<MatchRequest> requests = new ArrayList<>();

    protected VenueSlot() {}

    public VenueSlot(Venue venue, LocalDate slotDate, LocalTime startTime, LocalTime endTime, BigDecimal rent) {
        this.venue = venue;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rent = rent;
    }

    /** 實際場租：有即期優惠價就用優惠價。 */
    public BigDecimal getEffectiveRent() {
        return lastMinuteRent != null ? lastMinuteRent : rent;
    }

    public Integer getId() { return id; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public BigDecimal getRent() { return rent; }
    public void setRent(BigDecimal rent) { this.rent = rent; }
    public BigDecimal getLastMinuteRent() { return lastMinuteRent; }
    public void setLastMinuteRent(BigDecimal lastMinuteRent) { this.lastMinuteRent = lastMinuteRent; }
    public LocalDateTime getApplyDeadline() { return applyDeadline; }
    public void setApplyDeadline(LocalDateTime applyDeadline) { this.applyDeadline = applyDeadline; }
    public boolean isAllowCoPerformance() { return allowCoPerformance; }
    public void setAllowCoPerformance(boolean allowCoPerformance) { this.allowCoPerformance = allowCoPerformance; }
    public int getMaxPerformers() { return maxPerformers; }
    public void setMaxPerformers(int maxPerformers) { this.maxPerformers = maxPerformers; }
    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<MatchRequest> getRequests() { return requests; }
}
