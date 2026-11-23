package tw.gigradar.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tw.gigradar.dto.EventOut;
import tw.gigradar.repository.EventRepository;

/** 2.0 查詢與追蹤演出 —— 2.1 查詢演出。 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "2.0 演出查詢")
@Transactional(readOnly = true)
public class EventController {

    private final EventRepository events;

    public EventController(EventRepository events) {
        this.events = events;
    }

    @GetMapping
    public List<EventOut> search(
            @Parameter(description = "起始日期（預設今天）")
            @RequestParam(name = "date_from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "date_to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @Parameter(description = "地區，例：大安區") @RequestParam(required = false) String district,
            @RequestParam(name = "venue_id", required = false) Integer venueId,
            @Parameter(description = "表演者名稱或別名（部分符合）") @RequestParam(required = false) String performer,
            @RequestParam(required = false) String genre,
            @Parameter(description = "關鍵字（演出名稱）") @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        var spec = EventSpecs.searchEvents(dateFrom != null ? dateFrom : LocalDate.now(), dateTo, district, venueId,
                performer, genre, q);
        var page = PageRequest.of(0, limit, Sort.by("eventDate", "startTime"));
        return events.findAll(spec, page).map(EventOut::from).getContent();
    }

    @GetMapping("/{eventId}")
    public EventOut get(@PathVariable int eventId) {
        return events.findById(eventId)
                .map(EventOut::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到這場演出"));
    }
}
