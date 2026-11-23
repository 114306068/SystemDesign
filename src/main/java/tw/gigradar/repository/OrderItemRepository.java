package tw.gigradar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {}
