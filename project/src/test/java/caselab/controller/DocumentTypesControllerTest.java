package caselab.controller;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.service.types.DocumentTypesService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentTypesControllerTest extends BaseControllerIT {

    private final String DOCUMENT_TYPES_URI = "/api/v1/document_types";
    @MockBean
    private DocumentTypesService documentTypesService;
    @MockBean
    private SecurityFilterChain securityFilterChain;
    @Nested
    @Tag("Create")
    @DisplayName("Create document type")
    class CreateDocumentTypeTest {

        @SneakyThrows
        @Test
        @DisplayName("Should create document type with valid payload")
        public void createDocumentType_success() {
            var payload = new DocumentTypeRequest("test");
            var response = new DocumentTypeResponse(1L,payload.name());

            when(documentTypesService.createDocumentType(payload)).thenReturn(response);

            var mvcResponse = mockMvc.perform(post(DOCUMENT_TYPES_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpectAll(
                    status().is2xxSuccessful(),
                    content().contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse();

            var actualDocumentType = objectMapper.readValue(mvcResponse.getContentAsString(), DocumentTypeResponse.class);

            assertAll("Grouped assertions for created document type",
                () -> assertEquals(actualDocumentType.id(), response.id()),
                () -> assertEquals(actualDocumentType.name(), payload.name()));
        }
    }

    @Nested
    @Tag("GetById")
    @DisplayName("Get document type by id")
    class GetDocumentTypeByIdTest {

        @SneakyThrows
        @Test
        @DisplayName("Should return document type when it exists")
        public void getCategoryById_success() {
            var createdDocumentType = new DocumentTypeResponse(1L, "Test Document Type");

            when(documentTypesService.findDocumentTypeById(createdDocumentType.id())).thenReturn(createdDocumentType);

            var mvcResponse = mockMvc.perform(get(DOCUMENT_TYPES_URI + "/" + createdDocumentType.id()))
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

            var actualDocumentType = objectMapper.readValue(mvcResponse.getContentAsString(), DocumentTypeResponse.class);

            assertThat(actualDocumentType).isEqualTo(createdDocumentType);
        }
    }

    @Nested
    @Tag("Update")
    @DisplayName("Update document type")
    class UpdateCategoryTest {

        @SneakyThrows
        @Test
        @DisplayName("Should update document type when it exists")
        public void updateCategory_success() {
            var createdDocumentType = new DocumentTypeResponse(1L, "Old Name");
            var payload = new DocumentTypeRequest("New Name");

            when(documentTypesService.updateDocumentType(createdDocumentType.id(), payload))
                .thenReturn(new DocumentTypeResponse(createdDocumentType.id(), payload.name()));

            var mvcResponse = mockMvc.perform(patch(DOCUMENT_TYPES_URI + "/" + createdDocumentType.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse();

            var updatedDocumentType =
                objectMapper.readValue(mvcResponse.getContentAsString(), DocumentTypeResponse.class);

            assertAll(
                "Grouped assertions for updated document type",
                () -> assertEquals(updatedDocumentType.id(), createdDocumentType.id()),
                () -> assertEquals(updatedDocumentType.name(), payload.name())
            );
        }
    }

    @Nested
    @Tag("Delete")
    @DisplayName("Delete document type")
    class DeleteDocumentTypeTest {

        @SneakyThrows
        @Test
        @DisplayName("Should delete document type when it exists")
        public void deleteDocumentType_success() {
            var createdDocumentType = new DocumentTypeResponse(1L, "Test Document Type");

            var result = mockMvc.perform(delete(DOCUMENT_TYPES_URI + "/" +createdDocumentType.id())).andReturn();

            assertEquals(result.getResponse().getStatus(),200);
        }
    }

}
