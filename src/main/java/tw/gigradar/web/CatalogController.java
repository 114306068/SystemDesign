package tw.gigradar.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tw.gigradar.dto.PerformerOut;
import tw.gigradar.dto.VenueOut;
import tw.gigradar.repository.EventRepository;
import tw.gigradar.repository.PerformerRepository;
import tw.gigradar.repository.VenueRepository;

/** 場館與表演者的基本資料（D1），給查詢表單的下拉選單用。 */
@RestController
@RequestMapping("/api")
@Tag(name = "基本資料")
@Transactional(readOnly = true)
public class CatalogController {

    private final VenueRepository venues;
    private final PerformerRepository performers;
    private final EventRepository events;

    public CatalogController(VenueRepository venues, PerformerRepository performers, EventRepository events) {
        this.venues = venues;
        this.performers = performers;
        this.events = events;
    }

    @GetMapping("/venues")
    public List<VenueOut> venues(@RequestParam(required = false) String district) {
        var list = EventSpecs.has(district)
                ? venues.findByDistrictOrderByNameAsc(district)
                : venues.findAllByOrderByDistrictAscNameAsc();
        return list.stream().map(VenueOut::from).toList();
    }

    @GetMapping("/districts")
    public List<String> districts() {
        return venues.findDistinctDistricts();
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return events.findDistinctGenres();
    }

    @GetMapping("/performers")
    public List<PerformerOut> performers() {
        return performers.findAllByOrderByNameAsc().stream().map(PerformerOut::from).toList();
    }
}
