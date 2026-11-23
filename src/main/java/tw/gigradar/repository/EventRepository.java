package tw.gigradar.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import tw.gigradar.model.Event;

/** 演出查詢條件是動態組合的，所以另外繼承 JpaSpecificationExecutor（見 web.EventSpecs）。 */
public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    List<Event> findByVenueIdAndEventDate(Integer venueId, LocalDate eventDate);

    List<Event> findByEventDate(LocalDate eventDate);

    @Query("select distinct e.genre from Event e where e.genre is not null order by e.genre")
    List<String> findDistinctGenres();
}
