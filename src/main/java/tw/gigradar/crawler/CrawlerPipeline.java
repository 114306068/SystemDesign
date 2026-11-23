package tw.gigradar.crawler;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tw.gigradar.model.CrawlRun;
import tw.gigradar.model.CrawlSource;
import tw.gigradar.model.Event;
import tw.gigradar.model.Performer;
import tw.gigradar.model.Venue;
import tw.gigradar.model.enums.CrawlRunStatus;
import tw.gigradar.model.enums.EventSource;
import tw.gigradar.repository.CrawlRunRepository;
import tw.gigradar.repository.EventRepository;
import tw.gigradar.service.DedupService;
import tw.gigradar.service.NotifyService;

/** 爬蟲流程：1.1 擷取 → 1.2 整理格式 → 1.3 比對並寫入 D2。所有來源共用。 */
@Service
public class CrawlerPipeline {

    private static final Logger log = LoggerFactory.getLogger(CrawlerPipeline.class);

    private final Map<String, Source> sources;
    private final DedupService dedup;
    private final NotifyService notify;
    private final EventRepository events;
    private final CrawlRunRepository runs;
    private final TransactionTemplate tx;

    public CrawlerPipeline(List<Source> sources, DedupService dedup, NotifyService notify,
                           EventRepository events, CrawlRunRepository runs, PlatformTransactionManager txManager) {
        this.sources = sources.stream().collect(Collectors.toMap(Source::moduleName, Function.identity()));
        this.dedup = dedup;
        this.notify = notify;
        this.events = events;
        this.runs = runs;
        this.tx = new TransactionTemplate(txManager);
    }

    // ---------- 1.2 整理資料格式 ----------

    private static final Pattern FULL_DATE = Pattern.compile("(\\d{4})\\s*[/\\-.年]\\s*(\\d{1,2})\\s*[/\\-.月]\\s*(\\d{1,2})");
    private static final Pattern MONTH_DAY = Pattern.compile("(\\d{1,2})\\s*[/月]\\s*(\\d{1,2})");
    private static final Pattern TIME = Pattern.compile("(\\d{1,2})\\s*[:：點]\\s*(\\d{1,2})?");
    private static final Pattern AFTERNOON = Pattern.compile("(下午|晚上|晚間|pm|PM)");

    /** 支援 2026/12/05、2026-12-05、12月5日(六)、12/5。沒寫年份時取今天以後最近的那一天。 */
    public static LocalDate parseDate(String text, LocalDate today) {
        Matcher m = FULL_DATE.matcher(text);
        if (m.find()) {
            return LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        }
        m = MONTH_DAY.matcher(text);
        if (m.find()) {
            LocalDate d = LocalDate.of(today.getYear(), Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            return d.isBefore(today) ? d.plusYears(1) : d;
        }
        throw new IllegalArgumentException("無法解析日期：'" + text + "'");
    }

    /** 支援 19:30、7:30PM、晚上7:30、下午3點。沒寫或看不懂就回傳 null。 */
    public static LocalTime parseTime(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher m = TIME.matcher(text);
        if (!m.find()) return null;
        int hour = Integer.parseInt(m.group(1));
        int minute = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        if (hour < 12 && AFTERNOON.matcher(text).find()) hour += 12;
        return LocalTime.of(hour, minute);
    }

    /** 整理好格式的演出（日期時間已轉成標準型別）。 */
    public record CleanEvent(RawEvent raw, LocalDate eventDate, LocalTime startTime) {}

    public static CleanEvent normalize(RawEvent raw, LocalDate today) {
        try {
            return new CleanEvent(raw, parseDate(raw.dateText(), today), parseTime(raw.timeText()));
        } catch (DateTimeException e) { // 例如 13 月、2 月 30 日
            throw new IllegalArgumentException("日期時間不合理：" + raw.dateText() + " " + raw.timeText(), e);
        }
    }

    public static CleanEvent normalize(RawEvent raw) {
        return normalize(raw, LocalDate.now());
    }

    // ---------- 1.3 比對並寫入 ----------

    /** 一次寫入的統計。 */
    public static class RunResult {
        public int fetched;
        public int inserted;
        public int merged;
        public int notified;
        public final List<String> skipped = new ArrayList<>();
    }

    /** @param source 可為 null（例如測試、或之後表演者自行上架時共用這段邏輯） */
    @Transactional
    public RunResult upsertEvents(CrawlSource source, List<CleanEvent> cleaned) {
        RunResult result = new RunResult();
        result.fetched = cleaned.size();
        for (CleanEvent ce : cleaned) {
            RawEvent raw = ce.raw();
            Venue venue = dedup.findVenue(raw.venueName()).orElse(null);
            if (venue == null) {
                // 不自動建場館：交給管理者確認後新增場館或別名，避免同一場館被建成兩筆
                result.skipped.add("未知場館「" + raw.venueName() + "」：" + raw.title());
                continue;
            }
            List<Performer> performers = raw.performers().stream()
                    .map(name -> dedup.findOrCreatePerformer(name, raw.genre()))
                    .toList();

            Event existing = dedup.findDuplicateEvent(venue.getId(), ce.eventDate(), ce.startTime()).orElse(null);
            if (existing != null) {
                // 合併：補上缺的欄位、加入新出現的表演者
                if (existing.getStartTime() == null) existing.setStartTime(ce.startTime());
                if (existing.getTicketUrl() == null) existing.setTicketUrl(raw.ticketUrl());
                if (existing.getGenre() == null) existing.setGenre(raw.genre());
                for (Performer p : performers) {
                    if (!existing.getPerformers().contains(p)) existing.getPerformers().add(p);
                }
                result.merged++;
                continue;
            }

            Event event = new Event(raw.title(), venue, ce.eventDate(), ce.startTime(), EventSource.crawler);
            event.setGenre(raw.genre());
            event.setTicketUrl(raw.ticketUrl());
            event.setCrawlSource(source);
            event.setSourceUrl(raw.sourceUrl());
            event.getPerformers().addAll(performers);
            events.saveAndFlush(event);
            result.inserted++;
            result.notified += notify.notifyFollowers(event);
        }
        return result;
    }

    /** 跑一個來源，並把結果寫進 crawl_runs。任何錯誤都會留下紀錄，不會讓整支程式掛掉。 */
    public CrawlRun runSource(CrawlSource source) {
        LocalDateTime startedAt = LocalDateTime.now();
        CrawlRun run;
        try {
            run = tx.execute(status -> {
                Source impl = sources.get(source.getModule());
                if (impl == null) throw new IllegalStateException("找不到爬蟲模組：" + source.getModule());
                List<RawEvent> raws = impl.fetch();                                     // 1.1
                List<CleanEvent> cleaned = new ArrayList<>();
                List<String> problems = new ArrayList<>();
                for (RawEvent r : raws) {                                               // 1.2
                    try {
                        cleaned.add(normalize(r));
                    } catch (IllegalArgumentException e) {
                        problems.add(e.getMessage());
                    }
                }
                RunResult result = upsertEvents(source, cleaned);                       // 1.3
                problems.addAll(result.skipped);

                CrawlRun r = new CrawlRun(source, startedAt);
                r.setFetchedCount(raws.size());
                r.setInsertedCount(result.inserted);
                r.setMergedCount(result.merged);
                // 網站改版失效對策：抓到的筆數低於預期 → 標記異常
                r.setStatus(raws.size() < source.getExpectedMinCount() ? CrawlRunStatus.anomaly : CrawlRunStatus.success);
                r.setMessage(problems.isEmpty() ? null : String.join("\n", problems));
                r.setFinishedAt(LocalDateTime.now());
                return runs.save(r);
            });
        } catch (RuntimeException e) {
            // 上面的交易已整筆復原，另開一筆交易只記錄失敗
            CrawlRun failed = new CrawlRun(source, startedAt);
            failed.setStatus(CrawlRunStatus.failed);
            failed.setMessage(e.toString());
            failed.setFinishedAt(LocalDateTime.now());
            run = tx.execute(status -> runs.save(failed));
        }
        if (run.getStatus() != CrawlRunStatus.success) {
            // TODO：寄 Email 給維護者（提案書：異常時自動通知維護者）
            log.warn("[警告] 來源 {} 狀態 {}：{}", source.getName(), run.getStatus(), run.getMessage());
        }
        return run;
    }
}
