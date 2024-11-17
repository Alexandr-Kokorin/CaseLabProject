package caselab.service.document.template;

import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.storage.FileStorage;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.DocumentVersionNotFoundException;
import caselab.exception.template.IllFormedTemplateException;
import caselab.exception.template.NoTemplateException;
import caselab.service.util.DocumentUtilService;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class TemplateService {
    private final DocxService docxService;
    private final UserUtilService userService;
    private final DocumentUtilService documentUtilService;
    private final FileStorage fileStorage;
    private final DocumentTypesRepository documentTypesRepository;
    private final DocumentRepository documentRepository;

    @SneakyThrows
    public byte[] getTemplate(Long documentTypeId, Authentication auth) {
        var user = userService.findUserByAuthentication(auth);
        userService.checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        var docType = documentTypesRepository.findById(documentTypeId).orElseThrow(
            () -> new DocumentVersionNotFoundException(documentTypeId)
        );
        if (docType.getTemplateName() == null) {
            throw new NoTemplateException(documentTypeId);
        }

        // Эта штука кидает IOException
        // Кажется, кидать здесь (когда minio не может прочесть файл) error 500 вполне уместно
        return fileStorage.get(docType.getTemplateName()).readAllBytes();
    }

    public void setTemplate(Long documentTypeId, MultipartFile templateFile, Authentication auth) {
        var user = userService.findUserByAuthentication(auth);
        userService.checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        String filename = fileStorage.put(templateFile);
        var docType = documentTypesRepository.findById(documentTypeId)
            .orElseThrow(() -> new DocumentVersionNotFoundException(documentTypeId));
        docType.setTemplateName(filename);
        documentTypesRepository.save(docType);
    }

    public byte[] instantiateTemplate(Long documentId, Authentication auth) {
        var version = documentRepository
            .findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId))
            .getDocumentVersions()
            .getLast();
        return instantiateTemplateFromVersion(version, auth);
    }

    private byte[] instantiateTemplateFromVersion(DocumentVersion documentVersion, Authentication auth) {
        var user = userService.findUserByAuthentication(auth);
        documentUtilService.checkLacksPermission(user, documentVersion.getDocument(), DocumentPermissionName::canEdit);

        var docType = documentVersion.getDocument().getDocumentType();

        if (docType.getTemplateName() == null) {
            throw new NoTemplateException(docType.getId());
        }

        InputStream docxTemplate = fileStorage.get(docType.getTemplateName());

        try {
            return docxService.insertXml(docxTemplate, documentVersion.getAttributeValues().stream());
        } catch (Docx4JException e) {
            throw new IllFormedTemplateException(docType.getId());
        }
    }

    public void deleteTemplate(Long documentTypeId, Authentication auth) {
        var user = userService.findUserByAuthentication(auth);
        userService.checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        var docType = documentTypesRepository.findById(documentTypeId).orElseThrow(
            () -> new DocumentVersionNotFoundException(documentTypeId)
        );
        fileStorage.delete(docType.getTemplateName());
        docType.setTemplateName(null);
        documentTypesRepository.save(docType);
    }
}
