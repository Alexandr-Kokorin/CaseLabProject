package caselab.controller;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.service.DocumentTypesService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;

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
    class CreateCategoryTest {

        @SneakyThrows
        @Test
        @DisplayName("Should create document type with valid payload")
        public void createCategory_success() {
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

}
