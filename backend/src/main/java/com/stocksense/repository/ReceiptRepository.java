package com.stocksense.repository;
import com.stocksense.domain.Receipt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface ReceiptRepository extends JpaRepository<Receipt,Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Receipt r where r.id=:id") Optional<Receipt> lockById(@Param("id") Long id);
}
