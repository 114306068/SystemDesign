package tw.gigradar.web;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import tw.gigradar.model.Event;
import tw.gigradar.model.Performer;
import tw.gigradar.model.PerformerAlias;
import tw.gigradar.model.Venue;
import tw.gigradar.model.VenueSlot;
import tw.gigradar.model.enums.SlotStatus;

/**
 * 動態查詢條件（JPA Criteria）：使用者有填的欄位才加進 WHERE。
 * 等同於手寫 SQL：WHERE event_date >= ? [AND district = ?] [AND genre = ?] ...
 */
final class EventSpecs {

    private EventSpecs() {}

    static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    /** LIKE '%關鍵字%'，並跳脫使用者輸入裡的 % 與 _ */
    static String contains(String s) {
        return "%" + s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    static Specification<Event> searchEvents(LocalDate from, LocalDate to, String district, Integer venueId,
                                             String performer, String genre, String q) {
        return (root, query, cb) -> {
            Join<Event, Venue> venue = root.join("venue");
            List<Predicate> where = new ArrayList<>();
            where.add(cb.greaterThanOrEqualTo(root.get("eventDate"), from));
            if (to != null) where.add(cb.lessThanOrEqualTo(root.get("eventDate"), to));
            if (has(district)) where.add(cb.equal(venue.get("district"), district));
            if (venueId != null) where.add(cb.equal(venue.get("id"), venueId));
            if (has(genre)) where.add(cb.equal(root.get("genre"), genre));
            if (has(q)) where.add(cb.like(root.get("title"), contains(q), '\\'));
            if (has(performer)) {
                // 表演者名稱或別名任一符合（部分符合即可）
                Subquery<Integer> matched = query.subquery(Integer.class);
                Root<Performer> p = matched.from(Performer.class);
                Join<Performer, PerformerAlias> alias = p.join("aliases", JoinType.LEFT);
                String pattern = contains(performer);
                matched.select(p.get("id")).where(cb.or(
                        cb.like(p.get("name"), pattern, '\\'),
                        cb.like(alias.get("alias"), pattern, '\\')));
                where.add(root.join("performers").get("id").in(matched));
                query.distinct(true); // 共演場若兩團都符合，避免同一場出現兩次
            }
            return cb.and(where.toArray(Predicate[]::new));
        };
    }

    /** 3.2 可用空檔：只列 open、日期未過、且尚未超過申請時限的檔期。 */
    static Specification<VenueSlot> openSlots(LocalDate from, LocalDate to, String district, boolean coPerformanceOnly) {
        return (root, query, cb) -> {
            Join<VenueSlot, Venue> venue = root.join("venue");
            List<Predicate> where = new ArrayList<>();
            where.add(cb.equal(root.get("status"), SlotStatus.open));
            where.add(cb.greaterThanOrEqualTo(root.get("slotDate"), from));
            where.add(cb.or(
                    cb.isNull(root.get("applyDeadline")),
                    cb.greaterThanOrEqualTo(root.<LocalDateTime>get("applyDeadline"), LocalDateTime.now())));
            if (to != null) where.add(cb.lessThanOrEqualTo(root.get("slotDate"), to));
            if (has(district)) where.add(cb.equal(venue.get("district"), district));
            if (coPerformanceOnly) where.add(cb.isTrue(root.get("allowCoPerformance")));
            return cb.and(where.toArray(Predicate[]::new));
        };
    }
}
