package tw.gigradar.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.MatchRequest;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Integer> {

    List<MatchRequest> findBySlotIdOrderByCreatedAtAscIdAsc(Integer slotId);
}
