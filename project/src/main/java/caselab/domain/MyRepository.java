package caselab.domain;

import caselab.domain.entity.JpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyRepository extends JpaRepository<JpaEntity, Long> {
}
