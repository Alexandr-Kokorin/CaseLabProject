package caselab.domain.entity;

import caselab.domain.entity.enums.EventType;
import caselab.multitenancy.domain.entity.TenantAwareEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEvent extends TenantAwareEntity {

    private Long documentId;
    private String userEmail;
    private EventType eventType;
}
