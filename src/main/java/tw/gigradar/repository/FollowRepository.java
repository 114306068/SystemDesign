package tw.gigradar.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.Follow;
import tw.gigradar.model.enums.FollowTarget;

public interface FollowRepository extends JpaRepository<Follow, Integer> {

    List<Follow> findByTargetTypeAndVenueId(FollowTarget targetType, Integer venueId);

    List<Follow> findByTargetTypeAndPerformerIdIn(FollowTarget targetType, Collection<Integer> performerIds);

    List<Follow> findByTargetTypeAndGenre(FollowTarget targetType, String genre);
}
