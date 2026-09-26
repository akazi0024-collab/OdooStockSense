package com.stocksense.repository;
import com.stocksense.domain.Location;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface LocationRepository extends JpaRepository<Location,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select l from Location l where l.id=:id") Optional<Location> lockById(@Param("id") Long id);
}
