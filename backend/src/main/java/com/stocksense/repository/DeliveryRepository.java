package com.stocksense.repository;
import com.stocksense.domain.Delivery;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface DeliveryRepository extends JpaRepository<Delivery,Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select d from Delivery d where d.id=:id") Optional<Delivery> lockById(@Param("id") Long id);
}
