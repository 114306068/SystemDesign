package tw.gigradar.task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tw.gigradar.model.*;
import tw.gigradar.model.enums.ClaimStatus;
import tw.gigradar.model.enums.CommissionRuleType;
import tw.gigradar.model.enums.EventSource;
import tw.gigradar.model.enums.UserRole;
import tw.gigradar.repository.*;
import tw.gigradar.service.PasswordHasher;

/**
 * 示範資料：台北 7 間合作場館（虛構名稱）、表演者、演出、空檔、商品、抽成規則。
 * 日期都相對於今天產生，任何時候跑都看得到「近期演出」。seed 工作與測試都用這裡。
 */
@Component
public class DemoData {

    public static final String PASSWORD = "demo1234";

    private record VenueSeed(String name, String district, int capacity, List<String> aliases) {}

    private record PerformerSeed(String name, String genre, List<String> aliases) {}

    private static final List<VenueSeed> VENUES = List.of(
            new VenueSeed("北岸 Live House", "中正區", 400, List.of("北岸LIVE", "North Shore Live House")),
            new VenueSeed("公館地下室", "中正區", 120, List.of("Gongguan Basement")),
            new VenueSeed("大稻埕倉庫", "大同區", 250, List.of("Dadaocheng Warehouse")),
            new VenueSeed("信義聲響廳", "信義區", 600, List.of()),
            new VenueSeed("師大巷弄小劇場", "大安區", 80, List.of()),
            new VenueSeed("松菸邊角", "信義區", 200, List.of()),
            new VenueSeed("西門回聲", "萬華區", 300, List.of("Ximen Echo")));

    private static final List<PerformerSeed> PERFORMERS = List.of(
            new PerformerSeed("霧島樂隊", "indie", List.of("Kirishima Band", "霧島")),
            new PerformerSeed("藍調三重奏", "jazz", List.of("Blue Trio")),
            new PerformerSeed("午夜電台", "rock", List.of("Midnight Radio")),
            new PerformerSeed("小島民謠社", "folk", List.of()),
            new PerformerSeed("合成器少年", "electronic", List.of("Synth Boy")));

    private final UserRepository users;
    private final VenueRepository venues;
    private final PerformerRepository performers;
    private final EventRepository events;
    private final VenueSlotRepository slots;
    private final ProductRepository products;
    private final CommissionRuleRepository rules;
    private final FollowRepository follows;
    private final CrawlSourceRepository crawlSources;

    public DemoData(UserRepository users, VenueRepository venues, PerformerRepository performers,
                    EventRepository events, VenueSlotRepository slots, ProductRepository products,
                    CommissionRuleRepository rules, FollowRepository follows, CrawlSourceRepository crawlSources) {
        this.users = users;
        this.venues = venues;
        this.performers = performers;
        this.events = events;
        this.slots = slots;
        this.products = products;
        this.rules = rules;
        this.follows = follows;
        this.crawlSources = crawlSources;
    }

    @Transactional
    public void seed() {
        LocalDate today = LocalDate.now();

        User audience = users.save(new User("audience@demo.tw", "示範觀眾", UserRole.audience, PasswordHasher.hash(PASSWORD)));
        User band = users.save(new User("band@demo.tw", "霧島樂隊（帳號）", UserRole.performer, PasswordHasher.hash(PASSWORD)));
        User venueOwner = users.save(new User("venue@demo.tw", "北岸 Live House（帳號）", UserRole.venue, PasswordHasher.hash(PASSWORD)));
        User admin = users.save(new User("admin@demo.tw", "平台管理者", UserRole.admin, PasswordHasher.hash(PASSWORD)));

        List<Venue> v = new ArrayList<>();
        for (VenueSeed s : VENUES) {
            Venue venue = new Venue(s.name(), s.district());
            venue.setCapacity(s.capacity());
            venue.setAddress("台北市" + s.district() + "（示範地址）");
            s.aliases().forEach(venue::addAlias);
            v.add(venue);
        }
        v.get(0).setOwner(venueOwner);
        v.get(0).setClaimStatus(ClaimStatus.approved); // 階段二：已認領
        venues.saveAll(v);

        List<Performer> p = new ArrayList<>();
        for (PerformerSeed s : PERFORMERS) {
            Performer performer = new Performer(s.name(), s.genre());
            s.aliases().forEach(performer::addAlias);
            p.add(performer);
        }
        p.get(0).setUser(band);
        performers.saveAll(p);

        // 近期演出（表演者自行上架）：{幾天後, 場館, 表演者, 開演時間}；第 6 場為兩團共演
        int[][] plan = {{3, 0, 0}, {5, 1, 1}, {8, 2, 2}, {10, 3, 3}, {14, 4, 4}, {17, 6, 0}, {21, 2, 1}};
        String[] times = {"19:30", "20:00", "19:00", "15:00", "21:00", "19:30", "20:00"};
        for (int i = 0; i < plan.length; i++) {
            Performer performer = p.get(plan[i][2]);
            boolean coShow = i == 5;
            Event e = new Event(coShow ? "冬夜共演之夜" : performer.getName() + " 現場演出", v.get(plan[i][1]),
                    today.plusDays(plan[i][0]), LocalTime.parse(times[i]), EventSource.performer);
            e.setGenre(performer.getGenre());
            e.setTicketUrl("https://tickets.example.com/demo/" + (i + 1));
            e.getPerformers().addAll(coShow ? List.of(p.get(0), p.get(2)) : List.of(performer));
            events.save(e);
        }

        // 場地空檔：即期（一週內、有優惠價、有申請時限）與一般空檔 {場館, 幾天後, 場租, 即期價(0=無), 共演}
        int[][] slotPlan = {{0, 4, 30000, 18000, 0}, {1, 6, 12000, 8000, 1}, {3, 9, 45000, 0, 0}, {5, 12, 20000, 15000, 1}};
        for (int[] s : slotPlan) {
            LocalDate slotDate = today.plusDays(s[1]);
            VenueSlot slot = new VenueSlot(v.get(s[0]), slotDate, LocalTime.of(19, 0), LocalTime.of(22, 0), BigDecimal.valueOf(s[2]));
            slot.setLastMinuteRent(s[3] > 0 ? BigDecimal.valueOf(s[3]) : null);
            slot.setApplyDeadline(slotDate.minusDays(2).atTime(23, 59));
            slot.setAllowCoPerformance(s[4] == 1);
            slot.setMaxPerformers(s[4] == 1 ? 2 : 1);
            slots.save(slot);
        }

        products.saveAll(List.of(
                new Product(p.get(0), "霧島 巡演 T-shirt", BigDecimal.valueOf(650), 40),
                new Product(p.get(0), "霧島 首張 EP（CD）", BigDecimal.valueOf(400), 60),
                new Product(p.get(1), "藍調三重奏 帆布袋", BigDecimal.valueOf(350), 25)));

        rules.saveAll(List.of(
                new CommissionRule(CommissionRuleType.match, new BigDecimal("0.10"), today.minusDays(30), admin),
                new CommissionRule(CommissionRuleType.merch, new BigDecimal("0.10"), today.minusDays(30), admin)));

        // 示範觀眾追蹤霧島樂隊與 jazz 風格（跑爬蟲時會收到通知）
        follows.saveAll(List.of(Follow.performer(audience, p.get(0)), Follow.genre(audience, "jazz")));

        crawlSources.save(new CrawlSource("範例 JSON 來源", "sample_json", 3));
    }
}
