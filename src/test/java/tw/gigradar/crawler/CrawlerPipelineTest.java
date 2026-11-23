package tw.gigradar.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static tw.gigradar.crawler.CrawlerPipeline.normalize;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tw.gigradar.crawler.CrawlerPipeline.RunResult;
import tw.gigradar.model.Event;
import tw.gigradar.model.Notification;
import tw.gigradar.model.Performer;
import tw.gigradar.repository.EventRepository;
import tw.gigradar.repository.NotificationRepository;
import tw.gigradar.task.DemoData;

/** 1.3 比對寫入：去重合併、未知場館、通知追蹤者。 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CrawlerPipelineTest {

    private static final LocalDate DAY = LocalDate.of(2027, 1, 15);

    @Autowired CrawlerPipeline pipeline;
    @Autowired EventRepository events;
    @Autowired NotificationRepository notifications;
    @Autowired DemoData demoData;

    @BeforeEach
    void seed() {
        demoData.seed();
    }

    private static RawEvent raw(String venueName, String performer, String ticketUrl) {
        return new RawEvent("測試演出", venueName, "2027/01/15", "20:00", List.of(performer), "indie", ticketUrl, null);
    }

    @Test
    void duplicateFromTwoSourcesIsMerged() {
        RawEvent a = raw("北岸 LIVE HOUSE", "霧島樂隊", null);
        RawEvent b = raw("北岸Live House", "Kirishima Band", "https://t.example/1");
        RunResult result = pipeline.upsertEvents(null, List.of(normalize(a), normalize(b)));

        assertThat(result.inserted).isEqualTo(1);
        assertThat(result.merged).isEqualTo(1);
        List<Event> found = events.findByEventDate(DAY);
        assertThat(found).hasSize(1);
        Event ev = found.get(0);
        assertThat(ev.getTicketUrl()).isEqualTo("https://t.example/1");                    // 合併時補上缺的欄位
        assertThat(ev.getPerformers()).extracting(Performer::getName).containsExactly("霧島樂隊"); // 別名對到同一團
    }

    @Test
    void unknownVenueIsSkipped() {
        RunResult result = pipeline.upsertEvents(null, List.of(normalize(raw("不存在的場館", "霧島樂隊", null))));
        assertThat(result.inserted).isZero();
        assertThat(result.skipped).isNotEmpty();
    }

    @Test
    void newEventNotifiesFollowers() {
        pipeline.upsertEvents(null, List.of(normalize(raw("北岸 LIVE HOUSE", "霧島樂隊", null))));
        Event ev = events.findByEventDate(DAY).get(0);
        List<Notification> notes = notifications.findByEventId(ev.getId());
        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getMessage()).contains("霧島樂隊");
    }

    @Test
    void unknownPerformerIsCreated() {
        pipeline.upsertEvents(null, List.of(normalize(raw("公館地下室", "全新樂團", null))));
        assertThat(events.findByEventDate(DAY).get(0).getPerformers())
                .extracting(Performer::getName).containsExactly("全新樂團");
    }
}
