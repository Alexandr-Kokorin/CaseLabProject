package caselab.controller.document;

import caselab.controller.BaseControllerTest;
import caselab.controller.document.payload.DocumentAttributeValueDTO;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.service.document.DocumentService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        DocumentAttributeValueDTO attributeValueDTO = DocumentAttributeValueDTO.builder()
            .id(1L)
            .value("Test Value")
            .build();
        documentRequest = DocumentRequest.builder()
            .id(1L)
            .documentTypeId(1L)
            .applicationUserIds(Arrays.asList(1L, 2L))
            .attributeValues(Collections.singletonList(attributeValueDTO))
            .build();

        documentResponse = DocumentResponse.builder()
            .id(1L)
            .documentTypeId(1L)
            .applicationUserIds(Arrays.asList(1L, 2L))
            .attributeValues(Collections.singletonList(attributeValueDTO))
            .build();
    }

    @Tag("Create")
    @DisplayName("Should create document")
    @Test
    public void testCreateDocument() throws Exception {
        // Arrange
        when(documentService.createDocument(any(DocumentRequest.class))).thenReturn(documentResponse);

        // Act & Assert
        mockMvc.perform(post(DOCUMENT_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(documentRequest)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(documentResponse.id()),
                jsonPath("$.documentTypeId").value(documentResponse.documentTypeId()),
                jsonPath("$.applicationUserIds[0]").value(1),
                jsonPath("$.attributeValues[0].id").value(1),
                jsonPath("$.attributeValues[0].value").value("Test Value")
            );
    }

    @Tag("GetById")
    @DisplayName("Should return document for searching existing document")
    @Test
    public void testGetDocumentById() throws Exception {
        // Arrange
        when(documentService.getDocumentById(1L)).thenReturn(documentResponse);

        // Act & Assert
        mockMvc.perform(get(DOCUMENT_URI + "/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(documentResponse.id()),
                jsonPath("$.documentTypeId").value(documentResponse.documentTypeId())
            );
    }

    @Tag("GetById")
    @DisplayName("Should return NOT FOUND for searching not existing document")
    @Test
    public void testGetDocumentById_NotFound() throws Exception {
        // Arrange
        when(documentService.getDocumentById(1L)).thenThrow(new NoSuchElementException("Документ не найден"));

        // Act & Assert
        mockMvc.perform(get(DOCUMENT_URI + "/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Tag("Update")
    @DisplayName("Should update existing document by id")
    @Test
    public void testUpdateDocument() throws Exception {
        // Arrange
        when(documentService.updateDocument(eq(1L), any(DocumentRequest.class))).thenReturn(documentResponse);

        // Act & Assert
        mockMvc.perform(put(DOCUMENT_URI + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(documentResponse)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(documentResponse.id()),
                jsonPath("$.documentTypeId").value(documentResponse.documentTypeId())
            );
    }

    @Tag("Delete")
    @DisplayName("Should delete existing document by id")
    @Test
    public void testDeleteDocument() throws Exception {
        // Arrange
        doNothing().when(documentService).deleteDocument(1L);

        // Act & Assert
        mockMvc.perform(delete(DOCUMENT_URI + "/1"))
            .andExpect(status().isNoContent());
    }

    @Tag("Get All")
    @DisplayName("Should return All Documents")
    @Test
    public void testGetAllDocuments() throws Exception {
        // Arrange
        Page<DocumentResponse> documentPage = new PageImpl<>(List.of(documentResponse));
        when(documentService.getAllDocuments(any(Pageable.class))).thenReturn(documentPage);

        // Act & Assert
        mockMvc.perform(get(DOCUMENT_URI)
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.content[0].id").value(documentResponse.id()),
                jsonPath("$.content[0].documentTypeId").value(documentResponse.documentTypeId())
            );
    }

}
