package caselab.service.version;

import caselab.controller.version.payload.UpdateDocumentVersionRequest;
import caselab.domain.entity.DocumentVersion;
import caselab.service.version.mapper.DocumentVersionUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class DocumentVersionUpdaterTest {
    @Autowired
    private DocumentVersionUpdater updater;

    @Test
    public void testUpdate_normalUseCase() {
        var documentVersion = new DocumentVersion();
        documentVersion.setName("Old name");

        var newName = "new name";
        var request = new UpdateDocumentVersionRequest(newName);
        updater.update(request, documentVersion);

        assertEquals(newName, documentVersion.getName());
    }
}
