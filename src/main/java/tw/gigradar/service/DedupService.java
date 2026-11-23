package tw.gigradar.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.gigradar.model.Event;
import tw.gigradar.model.Performer;
import tw.gigradar.model.PerformerAlias;
import tw.gigradar.model.Venue;
import tw.gigradar.model.VenueAlias;
import tw.gigradar.repository.EventRepository;
import tw.gigradar.repository.PerformerAliasRepository;
import tw.gigradar.repository.PerformerRepository;
import tw.gigradar.repository.VenueAliasRepository;
import tw.gigradar.repository.VenueRepository;

/** 1.3 比對並寫入演出：去重與名稱比對。爬蟲與表演者上架都共用這裡。 */
@Service
@Transactional
public class DedupService {

    private final PerformerRepository performers;
    private final PerformerAliasRepository performerAliases;
    private final VenueRepository venues;
    private final VenueAliasRepository venueAliases;
    private final EventRepository events;

    public DedupService(PerformerRepository performers, PerformerAliasRepository performerAliases,
                        VenueRepository venues, VenueAliasRepository venueAliases, EventRepository events) {
        this.performers = performers;
        this.performerAliases = performerAliases;
        this.venues = venues;
        this.venueAliases = venueAliases;
        this.events = events;
    }

    /** 先比本名再比別名（都先正規化）。 */
    public Optional<Performer> findPerformer(String rawName) {
        String key = NameNormalizer.normalize(rawName);
        // 雛型資料量小，直接全表比對；資料量變大時改成存 normalized 欄位並加索引
        for (Performer p : performers.findAll()) {
            if (NameNormalizer.normalize(p.getName()).equals(key)) return Optional.of(p);
        }
        for (PerformerAlias a : performerAliases.findAll()) {
            if (NameNormalizer.normalize(a.getAlias()).equals(key)) return Optional.of(a.getPerformer());
        }
        return Optional.empty();
    }

    /** 找不到就新建一個表演者（之後可由樂團本人認領）。 */
    public Performer findOrCreatePerformer(String rawName, String genre) {
        return findPerformer(rawName)
                .orElseGet(() -> performers.saveAndFlush(new Performer(rawName.strip(), genre)));
    }

    public Optional<Venue> findVenue(String rawName) {
        String key = NameNormalizer.normalize(rawName);
        for (Venue v : venues.findAll()) {
            if (NameNormalizer.normalize(v.getName()).equals(key)) return Optional.of(v);
        }
        for (VenueAlias a : venueAliases.findAll()) {
            if (NameNormalizer.normalize(a.getAlias()).equals(key)) return Optional.of(a.getVenue());
        }
        return Optional.empty();
    }

    /**
     * 日期＋場館＋開演時間 相同 → 同一場。
     * 沒有開演時間時，同場館同日期只有一場才視為同一場（資料庫的 UNIQUE 擋不到 NULL，所以在程式裡再比一次）。
     */
    public Optional<Event> findDuplicateEvent(Integer venueId, LocalDate eventDate, LocalTime startTime) {
        List<Event> candidates = events.findByVenueIdAndEventDate(venueId, eventDate);
        if (startTime != null) {
            Optional<Event> exact = candidates.stream().filter(e -> startTime.equals(e.getStartTime())).findFirst();
            if (exact.isPresent()) return exact;
            List<Event> untimed = candidates.stream().filter(e -> e.getStartTime() == null).toList();
            return untimed.size() == 1 ? Optional.of(untimed.get(0)) : Optional.empty();
        }
        return candidates.size() == 1 ? Optional.of(candidates.get(0)) : Optional.empty();
    }
}
