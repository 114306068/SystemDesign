package tw.gigradar.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tw.gigradar.model.enums.EventSource;

/** D2 演出。 */
@Entity
@Table(
        name = "events",
        // 去重規則：日期＋場館＋開演時間 視為同一場
        uniqueConstraints = @UniqueConstraint(name = "uq_event_dedup", columnNames = {"venue_id", "event_date", "start_time"}),
        indexes = {
                @Index(name = "ix_events_event_date", columnList = "event_date"),
                @Index(name = "ix_events_genre", columnList = "genre")
        })
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private LocalDate eventDate;

    private LocalTime startTime;

    @Column(length = 50)
    private String genre;

    @Lob
    private String description;

    /** 購票導回原售票網站 */
    @Column(length = 500)
    private String ticketUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventSource sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawl_source_id")
    private CrawlSource crawlSource;

    /** 資料來源連結 */
    @Column(length = 500)
    private String sourceUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 一場演出可以有多組表演者（共演）→ 多對多，透過 event_performers 關聯表。 */
    @ManyToMany
    @JoinTable(
            name = "event_performers",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "performer_id"))
    private List<Performer> performers = new ArrayList<>();

    protected Event() {}

    public Event(String title, Venue venue, LocalDate eventDate, LocalTime startTime, EventSource sourceType) {
        this.title = title;
        this.venue = venue;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.sourceType = sourceType;
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTicketUrl() { return ticketUrl; }
    public void setTicketUrl(String ticketUrl) { this.ticketUrl = ticketUrl; }
    public EventSource getSourceType() { return sourceType; }
    public void setSourceType(EventSource sourceType) { this.sourceType = sourceType; }
    public CrawlSource getCrawlSource() { return crawlSource; }
    public void setCrawlSource(CrawlSource crawlSource) { this.crawlSource = crawlSource; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Performer> getPerformers() { return performers; }
}
