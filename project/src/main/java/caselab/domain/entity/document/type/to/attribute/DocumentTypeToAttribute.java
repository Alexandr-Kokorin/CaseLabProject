package caselab.domain.entity.document.type.to.attribute;

import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_type_to_attribute")
public class DocumentTypeToAttribute {
    @EmbeddedId
    private DocumentTypeToAttributeId id = new DocumentTypeToAttributeId();

    @Column(name = "is_optional", nullable = false)
    private boolean isOptional;

    @ManyToOne(optional = false)
    @MapsId("documentTypeId")
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @ManyToOne(optional = false)
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;
}
