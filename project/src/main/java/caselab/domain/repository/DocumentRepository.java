package caselab.domain.repository;

import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentVersion;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = {"applicationUsers"}) @NotNull
    Page<Document> findAll(@NotNull Pageable pageable);

    @EntityGraph(attributePaths = {"applicationUsers"}) @NotNull
    Optional<Document> findById(@NotNull Long id);
}
