package caselab.domain.entity;

import caselab.domain.entity.enums.DocumentStatus;
import caselab.elastic.listener.DocumentEventListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@EntityListeners(DocumentEventListener.class)
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @OneToMany(mappedBy = "document")
    @OrderBy("createdAt desc")
    private List<DocumentVersion> documentVersions;

    @OneToMany(mappedBy = "document")
    @ToString.Exclude
    private List<UserToDocument> usersToDocuments;
}
