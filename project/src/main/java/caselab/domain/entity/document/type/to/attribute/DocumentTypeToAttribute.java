package caselab.domain.entity.document.type.to.attribute;

import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentType;
import caselab.multitenancy.domain.entity.TenantAwareEntity;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_type_to_attribute")
public class DocumentTypeToAttribute extends TenantAwareEntity {

    @EmbeddedId
    private DocumentTypeToAttributeId id = new DocumentTypeToAttributeId();

    @Column(name = "is_optional", nullable = false)
    private Boolean isOptional;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId("documentTypeId")
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;
}
