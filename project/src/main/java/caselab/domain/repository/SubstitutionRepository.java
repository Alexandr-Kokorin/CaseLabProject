package caselab.domain.repository;

import caselab.domain.entity.Substitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubstitutionRepository extends JpaRepository<Substitution, Long> {
}
