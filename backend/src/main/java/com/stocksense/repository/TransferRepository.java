package com.stocksense.repository;
import com.stocksense.domain.StockTransfer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface TransferRepository extends JpaRepository<StockTransfer,Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from StockTransfer t where t.id=:id") Optional<StockTransfer> lockById(@Param("id") Long id);
}
