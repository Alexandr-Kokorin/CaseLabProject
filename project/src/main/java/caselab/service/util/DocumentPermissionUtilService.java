package caselab.service.util;

import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.document.version.MissingDocumentPermissionException;
import caselab.exception.status.DocumentStatusException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentPermissionUtilService {

    private final UserToDocumentRepository userToDocumentRepository;

    public boolean checkLacksPermission(
        ApplicationUser user,
        Document document,
        Predicate<DocumentPermissionName> permission
    ) {
        Optional<UserToDocument> userToDocument = userToDocumentRepository.findByApplicationUserIdAndDocumentId(
            user.getId(),
            document.getId()
        );
        return userToDocument.map(toDocument -> toDocument
            .getDocumentPermissions()
            .stream()
            .map(DocumentPermission::getName)
            .noneMatch(permission)).orElse(true);
    }

    public void assertHasPermission(
        ApplicationUser user,
        Document document,
        Predicate<DocumentPermissionName> permission,
        String message
    ) {
        if (checkLacksPermission(user, document, permission)) {
            throw new MissingDocumentPermissionException(message);
        }
    }

    public void assertHasDocumentStatus(
        Document document,
        List<DocumentStatus> statuses,
        DocumentStatusException exception
    ) {
        for (DocumentStatus status : statuses) {
            if (document.getStatus() == status) {
                return;
            }
        }
        throw exception;
    }
}
