package tw.gigradar.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import tw.gigradar.model.Event;

public record EventOut(Integer id, String title, LocalDate eventDate, LocalTime startTime, String genre,
                       String ticketUrl, String sourceType, String sourceUrl, LocalDateTime updatedAt,
                       VenueOut venue, List<PerformerOut> performers) {

    public static EventOut from(Event e) {
        return new EventOut(e.getId(), e.getTitle(), e.getEventDate(), e.getStartTime(), e.getGenre(),
                e.getTicketUrl(), e.getSourceType().name(), e.getSourceUrl(), e.getUpdatedAt(),
                VenueOut.from(e.getVenue()), e.getPerformers().stream().map(PerformerOut::from).toList());
    }
}
