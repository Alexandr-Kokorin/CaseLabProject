package caselab.service;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.domain.IntegrationTest;
import caselab.domain.repository.DocumentTypesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        var request =  new DocumentTypeRequest("test");

        var createdDocumentType = documentTypesService.createDocumentType(request);

        assertThat(createdDocumentType.id()).isNotNull();
        assertThat(createdDocumentType.name()).isEqualTo(request.name());
        assertThat(documentTypesRepository.existsById(createdDocumentType.id())).isEqualTo(true);
        assertThat(documentTypesRepository.findAll().size()).isEqualTo(1);
    }
    @Test
    @Transactional
    @Rollback
    public void findExistedDocumentTypeById(){
        var request = new DocumentTypeRequest("test");

        var createdDocumentType = documentTypesService.createDocumentType(request);
        var foundDocumentType = documentTypesService.findDocumentTypeById(createdDocumentType.id());

        assertThat(foundDocumentType).isNotNull();
        assertThat(foundDocumentType.name()).isEqualTo(request.name());
        assertThat(foundDocumentType.id()).isEqualTo(createdDocumentType.id());
    }
    @Test
    @Transactional
    @Rollback
    public void findNotExistedDocumentTypeById(){
        assertThrows(NoSuchElementException.class,()->documentTypesService.findDocumentTypeById(1L));
    }
    @Test
    @Transactional
    @Rollback
    public void deleteExistedDocumentTypeById(){
        var request = new DocumentTypeRequest("test");

        var createdDocumentType = documentTypesService.createDocumentType(request);

        assertDoesNotThrow(()->documentTypesService.deleteDocumentTypeById(createdDocumentType.id()));
        assertThat(documentTypesRepository.existsById(createdDocumentType.id())).isEqualTo(false);
        assertThat(documentTypesRepository.findAll().size()).isEqualTo(0);
    }
    @Test
    @Transactional
    @Rollback
    public void deleteBotExistedDocumentTypeById(){
        assertThrows(NoSuchElementException.class,()->documentTypesService.findDocumentTypeById(1L));
    }
}
