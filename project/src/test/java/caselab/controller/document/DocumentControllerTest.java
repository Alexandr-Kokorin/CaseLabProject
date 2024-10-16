package caselab.controller.document;

import caselab.controller.BaseControllerTest;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.exception.entity.DocumentNotFoundException;
import caselab.service.document.DocumentService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentControllerTest extends BaseControllerTest {

    private final String DOCUMENT_URI = "/api/v1/documents";

    @MockBean
    private DocumentService documentService;
    @MockBean
    private SecurityFilterChain securityFilterChain;

    private DocumentRequest documentRequest;
    private DocumentResponse documentResponse;

    @BeforeEach
    public void setup() {
        documentRequest = DocumentRequest.builder()
            .name("Test name")
            .documentTypeId(1L)
            .build();

        documentResponse = DocumentResponse.builder()
            .id(1L)
            .name("Test name")
            .documentTypeId(1L).build();
    }

    @Tag("Create")
    @DisplayName("Should create document")
    @Test
    public void testCreateDocument() throws Exception {
        // Arrange
        when(documentService.createDocument(any(DocumentRequest.class))).thenReturn(documentResponse);

        // Act & Assert
        mockMvc.perform(post(DOCUMENT_URI).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(documentRequest))).andExpectAll(
            status().isOk(),
            jsonPath("$.id").value(documentResponse.id()),
            jsonPath("$.name").value(documentResponse.name())
        );
    }

    @Tag("GetById")
    @DisplayName("Should return document for searching existing document")
    @Test
    public void testGetDocumentById() throws Exception {
        // Arrange
        when(documentService.getDocumentById(1L)).thenReturn(documentResponse);

        // Act & Assert
        mockMvc.perform(get(DOCUMENT_URI + "/1").accept(MediaType.APPLICATION_JSON)).andExpectAll(
            status().isOk(),
            jsonPath("$.id").value(documentResponse.id()),
            jsonPath("$.name").value(documentResponse.name())
        );
    }

    @Tag("GetById")
    @DisplayName("Should return NOT FOUND for searching not existing document")
    @Test
    public void testGetDocumentById_NotFound() throws Exception {
        // Arrange
        when(documentService.getDocumentById(1L)).thenThrow(new DocumentNotFoundException(1L));

        // Act & Assert
        mockMvc.perform(get(DOCUMENT_URI + "/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Tag("Update")
    @DisplayName("Should update existing document by id")
    @Test
    public void testUpdateDocument()
        throws Exception {
        // Arrange
        when(documentService.updateDocument(eq(1L), any(DocumentRequest.class))).thenReturn(documentResponse);

        // Act & Assert
        mockMvc.perform(put(DOCUMENT_URI + "/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(documentResponse)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(documentResponse.id()),
                jsonPath("$.name").value(documentResponse.name())
            );
    }

    @Tag("Delete")
    @DisplayName("Should delete existing document by id")
    @Test
    public void testDeleteDocument()
        throws Exception {
        // Arrange
        doNothing().when(documentService).deleteDocument(1L);

        // Act & Assert
        mockMvc.perform(delete(DOCUMENT_URI + "/1")).andExpect(status().isNoContent());
    }
/*
    @Tag("GetAll")
    @DisplayName("Should return All Documents")
    @Test
    public void testGetAllDocuments()
        throws Exception {
        // Arrange
        when(documentService.getAllDocuments()).thenReturn(List.of(documentResponse));

        // Act & Assert
        mockMvc.perform(get(DOCUMENT_URI).param("page", "0").param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.content[0].id").value(documentResponse.id()),
                jsonPath("$.content[0].documentTypeId").value(documentResponse.documentTypeId()),
                jsonPath("$.content[0].name").value(documentResponse.name())
            );
    }
*/
}
