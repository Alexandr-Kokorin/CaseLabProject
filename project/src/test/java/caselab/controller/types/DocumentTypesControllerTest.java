package caselab.controller.types;

import caselab.controller.BaseControllerTest;
import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.exception.EntityNotFoundException;
import caselab.service.types.DocumentTypesService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentTypesControllerTest extends BaseControllerTest {

    private static final String DOCUMENT_TYPES_URI = "/api/v1/document_types";
    private static final String NOT_FOUND = "Тип документа с id = %s не найден";

    // Предоставляем источник с некорректными запросами
    private static final Supplier<Stream<Arguments>> invalidDocumentTypeRequest = () -> Stream.of(
        Arguments.of(new DocumentTypeRequest(null, List.of())),
        Arguments.of(new DocumentTypeRequest("te", List.of())),
        Arguments.of(new DocumentTypeRequest("testtesttesttesttesttesttesttest", List.of()))
    );

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
            // Arrange
            var attributeRequests = List.of(
                new DocumentTypeToAttributeRequest(1L, true),
                new DocumentTypeToAttributeRequest(2L, false)
            );
            var payload = DocumentTypeRequest.builder()
                .name("test")
                .attributeRequests(attributeRequests)
                .build();

            var response = DocumentTypeResponse
                .builder()
                .name(payload.name())
                .id(1L)
                .build();

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

            var actualDocumentType =
                objectMapper.readValue(mvcResponse.getContentAsString(), DocumentTypeResponse.class);

            // Assert
            assertAll(
                "Grouped assertions for created document type",
                () -> assertEquals(actualDocumentType.id(), response.id()),
                () -> assertEquals(actualDocumentType.name(), payload.name())
            );
        }

        @SneakyThrows
        @ParameterizedTest
        @DisplayName("Should return 400 when request is invalid")
        @MethodSource("provideInvalidDocumentTypeRequest")
        public void createCategory_badRequest(DocumentTypeRequest documentTypeRequest) {
            mockMvc.perform(post(DOCUMENT_TYPES_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(documentTypeRequest)))
                .andExpect(
                    status().isBadRequest());
        }

        public static Stream<Arguments> provideInvalidDocumentTypeRequest() {
            return invalidDocumentTypeRequest.get();
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
            var createdDocumentType = DocumentTypeResponse
                .builder()
                .id(1L)
                .name("Test Document Type")
                .build();

            when(documentTypesService.findDocumentTypeById(createdDocumentType.id())).thenReturn(createdDocumentType);

            var mvcResponse = mockMvc.perform(get(DOCUMENT_TYPES_URI + "/" + createdDocumentType.id()))
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse();

            var actualDocumentType =
                objectMapper.readValue(mvcResponse.getContentAsString(), DocumentTypeResponse.class);

            assertEquals(actualDocumentType, createdDocumentType);
        }

        @SneakyThrows
        @Test
        @DisplayName("Should return 404 and error message when document type doesn't exist")
        public void getDocumentTypeById_notFound() {
            Long id = 1L;
            String errorMessage = NOT_FOUND.formatted(id);

            when(documentTypesService.findDocumentTypeById(id)).thenThrow(new EntityNotFoundException(errorMessage));

            mockMvc.perform(get(DOCUMENT_TYPES_URI + "/" + id))
                .andExpectAll(
                    status().isNotFound(),
                    content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                );
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
            var createdDocumentType = DocumentTypeResponse
                .builder()
                .id(1L)
                .name("Old Name")
                .build();

            var attributeRequests = List.of(
                new DocumentTypeToAttributeRequest(1L, true)
            );

            var payload = DocumentTypeRequest
                .builder()
                .name("New Name")
                .attributeRequests(attributeRequests)
                .build();

            when(documentTypesService.updateDocumentType(createdDocumentType.id(), payload))
                .thenReturn(DocumentTypeResponse.builder().id(createdDocumentType.id()).name(payload.name()).build());

            var mvcResponse = mockMvc.perform(put(DOCUMENT_TYPES_URI + "/" + createdDocumentType.id())
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

        @SneakyThrows
        @Test
        @DisplayName("Should return 404 and error message when updating non-existent document type")
        public void updateDocumentType_notFound() {
            Long id = 1L;
            String errorMessage = NOT_FOUND.formatted(id);
            var payload = new DocumentTypeRequest("New Name", List.of());

            when(documentTypesService.updateDocumentType(id, payload)).thenThrow(new EntityNotFoundException(errorMessage));

            mockMvc.perform(put(DOCUMENT_TYPES_URI + "/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpectAll(
                    status().isNotFound(),
                    content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                );
        }

        @SneakyThrows
        @ParameterizedTest
        @DisplayName("Should return 400 when request is invalid")
        @MethodSource("provideInvalidDocumentTypeRequest")
        public void createCategory_badRequest(DocumentTypeRequest documentTypeRequest) {
            mockMvc.perform(put(DOCUMENT_TYPES_URI + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(documentTypeRequest)))
                .andExpectAll(
                    status().isBadRequest());
        }

        public static Stream<Arguments> provideInvalidDocumentTypeRequest() {
            return invalidDocumentTypeRequest.get();
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
            var createdDocumentType = DocumentTypeResponse
                .builder()
                .id(1L)
                .name("Test Document Type")
                .build();

            var result = mockMvc.perform(delete(DOCUMENT_TYPES_URI + "/" + createdDocumentType.id())).andReturn();

            assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
        }
    }

}
