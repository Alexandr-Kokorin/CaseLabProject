package caselab.service.document.version;

import caselab.controller.document.version.payload.AttributeValueRequest;
import caselab.controller.document.version.payload.CreateDocumentVersionRequest;
import caselab.controller.document.version.payload.DocumentVersionResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.attribute.value.AttributeValue;
import caselab.domain.entity.attribute.value.AttributeValueId;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttributeId;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.AttributeValueRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.domain.storage.FileStorage;
import caselab.exception.document.version.MissingAttributesException;
import caselab.exception.document.version.MissingDocumentPermissionException;
import caselab.exception.entity.not_found.AttributeNotFoundException;
import caselab.exception.entity.not_found.DocumentVersionNotFoundException;
import caselab.service.document.version.mapper.DocumentVersionMapper;
import caselab.service.util.DocumentUtilService;
import caselab.service.util.UserUtilService;
import java.time.OffsetDateTime;
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
import org.springframework.security.core.Authentication;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class DocumentVersionServiceTest {
    @InjectMocks
    private DocumentVersionService service;

    @Mock
    private DocumentVersionRepository documentVersionRepository;
    @Mock
    private UserUtilService userUtilService;
    @Mock
    private DocumentUtilService documentUtilService;
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
    private FileStorage documentVersionStorage;

    private DocumentType documentType;
    private Document document;
    private ApplicationUser creator;
    private ApplicationUser reader;
    private ApplicationUser sender;
    private ApplicationUser stranger;
    private List<Attribute> attributes;
    private DocumentVersion documentVersion;

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

        stranger = new ApplicationUser();
        stranger.setId(4L);
        stranger.setEmail("stranger@email.com");
        stranger.setUsersToDocuments(List.of());

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

        documentVersion = new DocumentVersion();
        documentVersion.setId(1L);
        documentVersion.setAttributeValues(
            attributes
                .stream()
                .map(
                    at -> new AttributeValue(new AttributeValueId(1L, at.getId()), documentVersion, at, "" + at.getId())
                )
                .toList()
        );
        documentVersion.setName("documentVersion");
        documentVersion.setCreatedAt(OffsetDateTime.now());
        documentVersion.setContentName("/smth");
        documentVersion.setDocument(document);
        documentVersion.setSignatures(List.of());
        documentVersion.setVotingProcesses(List.of());
    }

    // TODO - что-нибудь сделать, сейчас не работает
    /*
    @Test
    public void createDocumentVersion_unauthorized() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(reader);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(reader.getUsersToDocuments().getFirst()));

        assertThrows(
            MissingDocumentPermissionException.class,
            () -> service.createDocumentVersion(new CreateDocumentVersionRequest(
                1L,
                List.of()
            ), null, (Authentication) null)
        );
    }

    @Test
    public void createDocumentVersion_missingAttributes() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(creator.getUsersToDocuments().getFirst()));

        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest(
            1L,
            List.of(
                new AttributeValueRequest(1L, ""),
                new AttributeValueRequest(3L, "")  // Не хватает аттрибута с id=2
            )
        );

        assertThrows(MissingAttributesException.class, () -> service.createDocumentVersion(request, null,
            (Authentication) null
        ));
    }

    @Test
    public void createDocumentVersion_unknownAttribute() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(creator.getUsersToDocuments().getFirst()));

        Mockito.when(attributeRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        for (long i = 0; i <= 1; ++i) {
            Mockito.when(attributeRepository.findById(i + 1)).thenReturn(Optional.of(attributes.get((int) i)));
        }

        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest(
            1L,
            List.of(
                new AttributeValueRequest(1L, ""),
                new AttributeValueRequest(2L, ""),
                new AttributeValueRequest(4L, "")  // Такого нет
            )
        );

        assertThrows(AttributeNotFoundException.class, () -> service.createDocumentVersion(request, null,
            (Authentication) null
        ));
    }

    @Test
    public void createDocumentVersion_success() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentRepository.findById(Mockito.any())).thenReturn(Optional.of(document));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(creator.getUsersToDocuments().getFirst()));

        Mockito.when(attributeRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        for (long i = 0; i <= 2; ++i) {
            Mockito.when(attributeRepository.findById(i + 1)).thenReturn(Optional.of(attributes.get((int) i)));
        }
        Mockito.when(documentVersionMapper.map(Mockito.any(DocumentVersion.class))).thenReturn(
            new DocumentVersionResponse()  // Главное, чтобы хоть какой-то был
        );
        Mockito.when(documentVersionRepository.save(Mockito.any(DocumentVersion.class))).thenAnswer(
            i -> i.getArguments()[0]
        );

        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest(
            1L,
            List.of(
                new AttributeValueRequest(1L, ""),
                new AttributeValueRequest(2L, ""),
                new AttributeValueRequest(3L, "")
            )
        );

        assertDoesNotThrow(() -> service.createDocumentVersion(request, null, (Authentication) null));
    }
     */

    @Test
    public void getDocumentVersionById_unknownDocumentVersion() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentVersionRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(
            DocumentVersionNotFoundException.class,
            () -> service.getDocumentVersionById(1L, (Authentication) null)
        );
    }

    @Test
    public void getDocumentVersionById_unauthorized() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(stranger);
        Mockito.when(documentVersionRepository.findById(Mockito.any())).thenReturn(Optional.of(documentVersion));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.empty());

        assertThrows(MissingDocumentPermissionException.class, () -> service.getDocumentVersionById(
            1L,
            (Authentication) null
        ));
    }

    @Test
    public void getDocumentVersionById_fullAccess() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(creator);
        Mockito.when(documentVersionRepository.findById(Mockito.any())).thenReturn(Optional.of(documentVersion));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(creator.getUsersToDocuments().getFirst()));

        var response = new DocumentVersionResponse();
        response.setAttributes(List.of());
        response.setContentName("/smth");

        Mockito.when(documentVersionMapper.map(documentVersion)).thenReturn(response);
        assertEquals(response, service.getDocumentVersionById(1L, (Authentication) null));
    }

    // TODO - что-нибудь сделать, сейчас не работает
    /*
    @Test
    public void getDocumentVersionById_partialAccess() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(sender);
        Mockito.when(documentVersionRepository.findById(Mockito.any())).thenReturn(Optional.of(documentVersion));
        Mockito.when(userToDocumentRepository.findByApplicationUserIdAndDocumentId(Mockito.any(), Mockito.any()))
            .thenReturn(Optional.of(sender.getUsersToDocuments().getFirst()));

        var response = new DocumentVersionResponse();
        response.setAttributes(List.of());
        response.setContentName("/smth");

        Mockito.when(documentVersionMapper.map(documentVersion)).thenReturn(response);
        var result = service.getDocumentVersionById(1L, (Authentication) null);
        assertNull(result.getAttributes());
        assertNull(result.getContentName());
    }

    @Test
    public void getDocumentVersions_notPresent() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(stranger);
        assertEquals(List.of(), service.getDocumentVersionsByDocumentId(null, null, null, null, null));
    }

    @Test
    public void getDocumentVersions_present() {
        Mockito.when(userUtilService.findUserByAuthentication(Mockito.any())).thenReturn(sender);
        Mockito.when(documentVersionMapper.map(documentVersion)).thenReturn(new DocumentVersionResponse());
        document.setDocumentVersions(List.of(documentVersion));
        assertEquals(1, service.getDocumentVersionsByDocumentId(null, null, null, null, null).getTotalElements());
    }
     */
}
