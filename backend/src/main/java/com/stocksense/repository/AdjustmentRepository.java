package com.stocksense.repository;
import com.stocksense.domain.StockAdjustment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface AdjustmentRepository extends JpaRepository<StockAdjustment,Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from StockAdjustment a where a.id=:id") Optional<StockAdjustment> lockById(@Param("id") Long id);
}
