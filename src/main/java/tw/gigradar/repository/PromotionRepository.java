package tw.gigradar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {}
