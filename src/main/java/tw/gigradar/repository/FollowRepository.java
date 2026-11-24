package tw.gigradar.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.Follow;
import tw.gigradar.model.enums.FollowTarget;

public interface FollowRepository extends JpaRepository<Follow, Integer> {

    List<Follow> findByTargetTypeAndVenueId(FollowTarget targetType, Integer venueId);

    List<Follow> findByTargetTypeAndPerformerIdIn(FollowTarget targetType, Collection<Integer> performerIds);

    List<Follow> findByTargetTypeAndGenre(FollowTarget targetType, String genre);

    // ---- 2.2 我的追蹤 ----

    List<Follow> findByUserIdOrderByCreatedAtDescIdDesc(Integer userId);

    Optional<Follow> findByIdAndUserId(Integer id, Integer userId);

    // 重複追蹤檢查（MySQL 的 UNIQUE 擋不到含 NULL 的列，所以在程式裡查）
    boolean existsByUserIdAndTargetTypeAndPerformerId(Integer userId, FollowTarget targetType, Integer performerId);

    boolean existsByUserIdAndTargetTypeAndVenueId(Integer userId, FollowTarget targetType, Integer venueId);

    boolean existsByUserIdAndTargetTypeAndGenre(Integer userId, FollowTarget targetType, String genre);
}
