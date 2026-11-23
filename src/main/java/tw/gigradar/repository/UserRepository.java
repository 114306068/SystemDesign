package tw.gigradar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {}
