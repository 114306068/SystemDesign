package tw.gigradar.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedAtDescIdDesc(Integer userId);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDescIdDesc(Integer userId);

    List<Notification> findByEventId(Integer eventId);
}
