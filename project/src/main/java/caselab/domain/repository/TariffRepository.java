package caselab.domain.repository;

import caselab.domain.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findByName(String name);
}
