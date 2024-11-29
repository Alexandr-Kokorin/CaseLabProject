package caselab.domain.repository;

import caselab.domain.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findAllByIssuedAtBefore(LocalDateTime cutoffDate);

    List<Bill> findAllByIsPaidFalse();
}
