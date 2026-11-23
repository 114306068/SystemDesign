package tw.gigradar.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.CrawlSource;

public interface CrawlSourceRepository extends JpaRepository<CrawlSource, Integer> {

    List<CrawlSource> findByActiveTrue();

    List<CrawlSource> findByActiveTrueAndModuleIn(Collection<String> modules);
}
