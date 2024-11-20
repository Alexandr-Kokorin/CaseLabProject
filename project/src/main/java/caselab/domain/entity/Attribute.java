package caselab.domain.entity;

import caselab.domain.entity.attribute.value.AttributeValue;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.multitenancy.domain.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
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
@Table(name = "attribute")
public class Attribute extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @OneToMany(mappedBy = "attribute")
    private List<AttributeValue> attributeValues;

    @OneToMany(mappedBy = "attribute")
    private List<DocumentTypeToAttribute> documentTypesToAttributes;
}
