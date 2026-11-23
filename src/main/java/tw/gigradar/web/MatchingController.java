package tw.gigradar.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tw.gigradar.dto.MatchRequestOut;
import tw.gigradar.dto.SlotOut;
import tw.gigradar.repository.MatchRequestRepository;
import tw.gigradar.repository.VenueSlotRepository;

/**
 * 3.0 管理檔期與預訂。
 *
 * <p>已完成：3.2 查詢可用空檔、查看某空檔的申請。
 * <p>待實作（雛型開發 I 階段）：
 * <ul>
 *   <li>3.1 POST /api/slots                    場地方上架空檔（需已認領場館）</li>
 *   <li>3.3 POST /api/slots/{id}/requests      表演者提出申請</li>
 *   <li>3.4 POST /api/requests/{id}/accept     場地方確認 → slot.status = booked</li>
 *   <li>3.5 （3.4 內呼叫）建立 Event(sourceType = match)，並呼叫 NotifyService.notifyFollowers</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/slots")
@Tag(name = "3.0 場地媒合")
@Transactional(readOnly = true)
public class MatchingController {

    private final VenueSlotRepository slots;
    private final MatchRequestRepository requests;

    public MatchingController(VenueSlotRepository slots, MatchRequestRepository requests) {
        this.slots = slots;
        this.requests = requests;
    }

    @GetMapping
    @Operation(summary = "3.2 查詢可用空檔：只列 open、且尚未超過申請時限的檔期")
    public List<SlotOut> openSlots(
            @RequestParam(name = "date_from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "date_to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String district,
            @RequestParam(name = "co_performance_only", defaultValue = "false") boolean coPerformanceOnly) {
        var spec = EventSpecs.openSlots(dateFrom != null ? dateFrom : LocalDate.now(), dateTo, district, coPerformanceOnly);
        return slots.findAll(spec, Sort.by("slotDate", "startTime")).stream().map(SlotOut::from).toList();
    }

    @GetMapping("/{slotId}/requests")
    public List<MatchRequestOut> requests(@PathVariable int slotId) {
        if (!slots.existsById(slotId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到這個檔期");
        }
        return requests.findBySlotIdOrderByCreatedAtAscIdAsc(slotId).stream().map(MatchRequestOut::from).toList();
    }
}
