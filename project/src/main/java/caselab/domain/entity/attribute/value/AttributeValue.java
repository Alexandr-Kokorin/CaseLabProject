package caselab.domain.entity.attribute.value;

import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentVersion;
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
@Table(name = "document_attribute_value")
public class AttributeValue {

    @EmbeddedId
    private AttributeValueId id = new AttributeValueId();

    @ManyToOne
    @MapsId("documentId")
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;

    @ManyToOne
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(name = "app_value")
    private String appValue;
}
