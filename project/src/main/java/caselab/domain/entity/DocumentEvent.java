package caselab.domain.entity;

import caselab.domain.entity.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEvent {

    private Long documentVersionId;
    private String userEmail;
    private EventType eventType;
}