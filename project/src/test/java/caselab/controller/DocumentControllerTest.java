package caselab.controller;

import caselab.controller.document.payload.DocumentAttributeValueDTO;
import caselab.controller.document.payload.DocumentDTO;
import caselab.controller.document.payload.DocumentResponseDTO;
import caselab.domain.entity.exception.ResourceNotFoundException;
import caselab.service.document.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentControllerTest extends BaseControllerMockTest {

    private final String DOCUMENT_URI = "/api/v1/documents";

    @MockBean
    private DocumentService documentService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    private DocumentDTO documentDTO;
    private DocumentResponseDTO documentDTOResponse;

    @BeforeEach
    public void setup() {
        documentDTO = new DocumentDTO();
        documentDTO.setId(1L);
        documentDTO.setDocumentTypeId(1L);
        documentDTO.setApplicationUserIds(Arrays.asList(1L, 2L));
        DocumentAttributeValueDTO attributeValueDTO = new DocumentAttributeValueDTO();
        attributeValueDTO.setId(1L);
        attributeValueDTO.setValue("Test Value");
        documentDTO.setAttributeValues(Collections.singletonList(attributeValueDTO));
        documentDTOResponse = new DocumentResponseDTO();
        documentDTOResponse.setId(1L);
        documentDTOResponse.setDocumentTypeId(1L);
        documentDTOResponse.setApplicationUserIds(Arrays.asList(1L, 2L));
        documentDTOResponse.setAttributeValues(Collections.singletonList(attributeValueDTO));
    }

    @Tag("Create")
    @DisplayName("Should create document")
    @Test
    public void testCreateDocument() throws Exception {
        // Arrange
        when(documentService.createDocument(any(DocumentDTO.class))).thenReturn(documentDTOResponse);

        // Act & Assert
        mockMvc.perform(post(DOCUMENT_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(documentDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentDTOResponse.getId()))
            .andExpect(jsonPath("$.documentTypeId").value(documentDTOResponse.getDocumentTypeId()))
            .andExpect(jsonPath("$.applicationUserIds[0]").value(1))
            .andExpect(jsonPath("$.attributeValues[0].id").value(1))
            .andExpect(jsonPath("$.attributeValues[0].value").value("Test Value"));
    }

    @Tag("GetById")
    @DisplayName("Should return document for searching existing document")
    @Test
    public void testGetDocumentById() throws Exception {
        // Arrange
        when(documentService.getDocumentById(1L)).thenReturn(documentDTOResponse);

        // Act & Assert
        mockMvc.perform(get(DOCUMENT_URI + "/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentDTOResponse.getId()))
            .andExpect(jsonPath("$.documentTypeId").value(documentDTOResponse.getDocumentTypeId()));
    }

    @Tag("GetById")
    @DisplayName("Should return NOT FOUND for searching not existing document")
    @Test
    public void testGetDocumentById_NotFound() throws Exception {
        // Arrange
        when(documentService.getDocumentById(1L)).thenThrow(new ResourceNotFoundException("Документ не найден"));

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
        when(documentService.updateDocument(eq(1L), any(DocumentDTO.class))).thenReturn(documentDTOResponse);

        // Act & Assert
        mockMvc.perform(put(DOCUMENT_URI + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(documentDTOResponse)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentDTOResponse.getId()))
            .andExpect(jsonPath("$.documentTypeId").value(documentDTOResponse.getDocumentTypeId()));
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

}
