package tw.gigradar.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.Performer;

public interface PerformerRepository extends JpaRepository<Performer, Integer> {

    List<Performer> findAllByOrderByNameAsc();
}
