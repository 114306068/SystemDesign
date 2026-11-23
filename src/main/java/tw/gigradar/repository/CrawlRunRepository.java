package tw.gigradar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.CrawlRun;

public interface CrawlRunRepository extends JpaRepository<CrawlRun, Integer> {}
