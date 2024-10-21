package caselab.service.version;

import caselab.controller.version.payload.AttributeValuePair;
import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.controller.version.payload.DocumentVersionResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttributeId;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.AttributeValueRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.document.version.MissingAttributesException;
import caselab.exception.document.version.MissingDocumentPermissionException;
import caselab.exception.entity.AttributeNotFoundException;
import caselab.service.users.ApplicationUserService;
import caselab.service.version.mapper.DocumentVersionMapper;
import caselab.service.version.mapper.DocumentVersionUpdater;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class DocumentVersionServiceTest {
    @InjectMocks
    private DocumentVersionService service;

    @Mock
    private DocumentVersionRepository documentVersionRepository;
    @Mock
    private ApplicationUserService userService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private UserToDocumentRepository userToDocumentRepository;
    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private AttributeValueRepository attributeValueRepository;
    @Mock
    private DocumentVersionMapper documentVersionMapper;
    @Mock
    private DocumentVersionUpdater documentVersionUpdater;

    private DocumentType documentType;
    private Document document;
    private ApplicationUser creator;
    private ApplicationUser reader;
    private ApplicationUser sender;
    private List<Attribute> attributes;

    @BeforeEach
    public void setUp() {
        documentType = new DocumentType();
        documentType.setId(1L);
        documentType.setName("document type");

        document = new Document();
        document.setId(1L);
        document.setName("document");
        document.setDocumentType(documentType);

        creator = new ApplicationUser();
        creator.setId(1L);
        creator.setEmail("creator@email.com");
        creator.setUsersToDocuments(
            List.of(new UserToDocument(
                1L,
                creator,
                document,
                List.of(new DocumentPermission(1L, DocumentPermissionName.CREATOR))
            ))
        );

        reader = new ApplicationUser();
        reader.setId(2L);
        reader.setEmail("reader@email.com");
        reader.setUsersToDocuments(
            List.of(new UserToDocument(
                2L,
                reader,
                document,
                List.of(new DocumentPermission(2L, DocumentPermissionName.READ))
            ))
        );

        sender = new ApplicationUser();
        sender.setId(3L);
        sender.setEmail("sender@email.com");
        sender.setUsersToDocuments(
            List.of(new UserToDocument(
                3L,
                sender,
                document,
                List.of(new DocumentPermission(3L, DocumentPermissionName.SEND_FOR_SIGNING))
            ))
        );

        document.setUsersToDocuments(
            Stream.of(creator, reader, sender)
                .flatMap(user -> user.getUsersToDocuments().stream())
                .toList()
        );

        attributes = new ArrayList<>();
        var documentTypeToAttributes = LongStream.of(1L, 2L, 3L).mapToObj(
            i -> {
                var res = new DocumentTypeToAttribute();
                res.setId(new DocumentTypeToAttributeId(documentType.getId(), i));
                res.setIsOptional(i == 3L);
                res.setDocumentType(documentType);
                var attr = new Attribute(i, "" + i, "Long", null, null);
                attributes.add(attr);
                res.setAttribute(attr);
                return res;
            }
        ).toList();
        documentType.setDocumentTypesToAttributes(documentTypeToAttributes);
    }

    @Test
    public void createDocumentVersion_unauthorized() {
        Mockito.when(userService.findUserByAuthentication(Mockito.any())).thenReturn(reader);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(reader.getUsersToDocuments().getFirst()));

        assertThrows(
            MissingDocumentPermissionException.class,
            () -> service.createDocumentVersion(new CreateDocumentVersionRequest(
                1L,
                "",
                "",
                List.of()
            ), null)
        );
    }

    @Test
    public void createDocumentVersion_missingAttributes() {
        Mockito.when(userService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(creator.getUsersToDocuments().getFirst()));

        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest(
            1L,
            "",
            "",
            List.of(
                new AttributeValuePair(1L, ""),
                new AttributeValuePair(3L, "")  // Не хватает аттрибута с id=2
            )
        );

        assertThrows(MissingAttributesException.class, () -> service.createDocumentVersion(request, null));
    }

    @Test
    public void createDocumentVersion_unknownAttribute() {
        Mockito.when(userService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(creator.getUsersToDocuments().getFirst()));

        Mockito.when(attributeRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        for (long i = 0; i <= 1; ++i) {
            Mockito.when(attributeRepository.findById(i + 1)).thenReturn(Optional.of(attributes.get((int)i)));
        }

        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest(
            1L,
            "",
            "",
            List.of(
                new AttributeValuePair(1L, ""),
                new AttributeValuePair(2L, ""),
                new AttributeValuePair(4L, "")  // Такого нет
            )
        );

        assertThrows(AttributeNotFoundException.class, () -> service.createDocumentVersion(request, null));
    }

    @Test
    public void createDocumentVersion_success() {
        Mockito.when(userService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(creator.getUsersToDocuments().getFirst()));

        Mockito.when(attributeRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        for (long i = 0; i <= 2; ++i) {
            Mockito.when(attributeRepository.findById(i + 1)).thenReturn(Optional.of(attributes.get((int)i)));
        }
        Mockito.when(documentVersionMapper.map(Mockito.any(DocumentVersion.class))).thenReturn(
            new DocumentVersionResponse()  // Главное, чтобы хоть какой-то был
        );
        Mockito.when(documentVersionRepository.save(Mockito.any(DocumentVersion.class))).thenAnswer(
            i -> i.getArguments()[0]
        );

        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest(
            1L,
            "",
            "",
            List.of(
                new AttributeValuePair(1L, ""),
                new AttributeValuePair(2L, ""),
                new AttributeValuePair(3L, "")
            )
        );

        assertDoesNotThrow(() -> service.createDocumentVersion(request, null));
    }
}
