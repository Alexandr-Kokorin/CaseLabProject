package caselab.domain.entity.attribute.value;

import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.TenantAwareEntity;
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
@Table(name = "document_attribute_value")
public class AttributeValue extends TenantAwareEntity {

    @EmbeddedId
    private AttributeValueId id = new AttributeValueId();

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId("documentVersionId")
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(name = "app_value")
    private String appValue;
}
