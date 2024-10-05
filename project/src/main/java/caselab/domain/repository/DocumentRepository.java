package caselab.domain.repository;

import caselab.domain.entity.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = {"applicationUsers", "attributeValues.attribute"}) @NotNull
    Page<Document> findAll(@NotNull Pageable pageable);

    @EntityGraph(attributePaths = {"applicationUsers", "attributeValues.attribute"}) @NotNull
    Optional<Document> findById(@NotNull Long id);
}
