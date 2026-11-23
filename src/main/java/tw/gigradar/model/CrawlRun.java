package tw.gigradar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import tw.gigradar.model.enums.CrawlRunStatus;

/** D2 每次抓取的紀錄，用來監控網站改版造成的失效。 */
@Entity
@Table(name = "crawl_runs")
public class CrawlRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private CrawlSource source;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private int fetchedCount;

    @Column(nullable = false)
    private int insertedCount;

    @Column(nullable = false)
    private int mergedCount;

    @Enumerated(EnumType.STRING)
    private CrawlRunStatus status;

    @Lob
    private String message;

    protected CrawlRun() {}

    public CrawlRun(CrawlSource source, LocalDateTime startedAt) {
        this.source = source;
        this.startedAt = startedAt;
    }

    public Integer getId() { return id; }
    public CrawlSource getSource() { return source; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public int getFetchedCount() { return fetchedCount; }
    public void setFetchedCount(int fetchedCount) { this.fetchedCount = fetchedCount; }
    public int getInsertedCount() { return insertedCount; }
    public void setInsertedCount(int insertedCount) { this.insertedCount = insertedCount; }
    public int getMergedCount() { return mergedCount; }
    public void setMergedCount(int mergedCount) { this.mergedCount = mergedCount; }
    public CrawlRunStatus getStatus() { return status; }
    public void setStatus(CrawlRunStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
