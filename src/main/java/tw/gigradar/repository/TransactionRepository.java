package tw.gigradar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tw.gigradar.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {}
