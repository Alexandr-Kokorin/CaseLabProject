package caselab.domain.repository;

import caselab.domain.entity.Signature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
}
