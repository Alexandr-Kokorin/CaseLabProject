package caselab.domain.repository;

import caselab.domain.entity.Bill;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findAllByPaidUntilBefore(LocalDateTime now);

    Optional<Bill> findByUserOrganizationId(Long organizationId);
}
