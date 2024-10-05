package caselab.service;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.domain.IntegrationTest;
import caselab.domain.repository.DocumentTypesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class DocumentTypesServiceTest extends IntegrationTest {
    @Autowired
    private DocumentTypesService documentTypesService;

    @Autowired
    private DocumentTypesRepository documentTypesRepository;

    @Test
    @Transactional
    @Rollback
    public void createDocumentTypeValid(){
        DocumentTypeRequest documentTypeRequest =  new DocumentTypeRequest("test");

        DocumentTypeResponse createdDocumentTypeDTO = documentTypesService.createDocumentType(documentTypeRequest);

        assertThat(createdDocumentTypeDTO.id()).isNotNull();
        assertThat(createdDocumentTypeDTO.name()).isEqualTo(documentTypeRequest.name());
        assertThat(documentTypesRepository.existsById(createdDocumentTypeDTO.id())).isEqualTo(true);
        assertThat(documentTypesRepository.findAll().size()).isEqualTo(1);
    }
}
