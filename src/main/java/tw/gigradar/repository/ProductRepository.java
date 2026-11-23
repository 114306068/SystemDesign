package tw.gigradar.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByPerformerIdAndActiveTrueOrderByIdAsc(Integer performerId);
}
