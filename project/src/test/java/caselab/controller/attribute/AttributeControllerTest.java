package caselab.controller.attribute;

import caselab.controller.BaseControllerTest;
import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.Attribute;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AttributeControllerTest extends BaseControllerTest {

    private static final Supplier<Stream<Arguments>> invalidAttributeRequests = () -> Stream.of(
        Arguments.of(new AttributeRequest(null, null)),
        Arguments.of(new AttributeRequest("", "")),
        Arguments.of(new AttributeRequest("    ", "    ")),
        Arguments.of(new AttributeRequest("name", null)),
        Arguments.of(new AttributeRequest("name", "")),
        Arguments.of(new AttributeRequest("name", "    ")),
        Arguments.of(new AttributeRequest(null, "type")),
        Arguments.of(new AttributeRequest("", "type")),
        Arguments.of(new AttributeRequest("    ", "type")),
        Arguments.of(new AttributeRequest("a".repeat(26), "type")),
        Arguments.of(new AttributeRequest("na", "type"))
    );

    private final String ATTRIBUTE_URI = "/api/v1/attributes";

    @MockBean
    private SecurityFilterChain securityFilterChain;

    public static Stream<Arguments> provideInvalidAttributeRequests() {
        return invalidAttributeRequests.get();
    }

    @Test
    @SneakyThrows
    public void testCreateAttribute_whenAttributeRequestIsValid_shouldReturnCreatedAttribute() {
        var attributeRequest = AttributeRequest.builder()
            .name("name")
            .type("type")
            .build();

        var mvcResponse = mockMvc.perform(post(ATTRIBUTE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn()
            .getResponse();

        var attributeResponse = objectMapper.readValue(mvcResponse.getContentAsString(), AttributeResponse.class);

        assertAll(
            () -> assertEquals(attributeRequest.name(), attributeResponse.name()),
            () -> assertEquals(attributeRequest.type(), attributeResponse.type())
        );

        deleteAttribute(attributeResponse.id());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideInvalidAttributeRequests")
    public void testCreateAttribute_whenAttributeRequestIsInvalid(AttributeRequest attributeRequest) {
        mockMvc.perform(post(ATTRIBUTE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }

    @Test
    @SneakyThrows
    public void testFindAttributeById_whenAttributeExists_shouldReturnAttributeById() {
        var existingAttribute = createAttribute("name", "type");

        var mvcResponse = mockMvc.perform(get(ATTRIBUTE_URI + "/" + existingAttribute.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn()
            .getResponse();

        var attributeResponse = objectMapper.readValue(mvcResponse.getContentAsString(), AttributeResponse.class);

        assertAll(
            () -> assertEquals(existingAttribute.getId(), attributeResponse.id()),
            () -> assertEquals(existingAttribute.getName(), attributeResponse.name()),
            () -> assertEquals(existingAttribute.getType(), attributeResponse.type())
        );

        deleteAttribute(existingAttribute.getId());
    }

    @Test
    @SneakyThrows
    public void testFindAttributeById_whenAttributeNotFound() {
        mockMvc.perform(get(ATTRIBUTE_URI + "/1"))
            .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }

    @Test
    @SneakyThrows
    public void testFindAllAttributes_whenAtLeastOneAttributeExists_shouldReturnListOfAttributes() {
        var existingAttribute = createAttribute("name", "type");

        var mvcResponse = mockMvc.perform(get(ATTRIBUTE_URI)
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn()
            .getResponse();

        var attributes = List.of(objectMapper.readValue(mvcResponse.getContentAsString(), AttributeResponse[].class));

        assertAll(
            () -> assertEquals(1, attributes.size()),
            () -> assertEquals(existingAttribute.getId(), attributes.getFirst().id()),
            () -> assertEquals(existingAttribute.getName(), attributes.getFirst().name()),
            () -> assertEquals(existingAttribute.getType(), attributes.getFirst().type())
        );
    }

    @Test
    @SneakyThrows
    public void testFindAllAttributes_whenThereIsNoAttribute_shouldReturnEmptyList() {

        var mvcResponse = mockMvc.perform(get(ATTRIBUTE_URI)
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn()
            .getResponse();

        var attributes = List.of(objectMapper.readValue(mvcResponse.getContentAsString(), AttributeResponse[].class));

        assertTrue(attributes.isEmpty());
    }

    @Test
    @SneakyThrows
    public void testUpdateAttribute_whenAttributeExists_shouldReturnUpdatedAttribute() {
        var existingAttribute = createAttribute("name", "type");

        var attributeRequest = AttributeRequest.builder()
            .name("newName")
            .type("newType")
            .build();

        var mvcResponse = mockMvc.perform(put(ATTRIBUTE_URI + "/" + existingAttribute.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn()
            .getResponse();

        var updatedAttribute = objectMapper.readValue(mvcResponse.getContentAsString(), AttributeResponse.class);

        assertAll(
            () -> assertEquals(existingAttribute.getId(), updatedAttribute.id()),
            () -> assertEquals(attributeRequest.name(), updatedAttribute.name()),
            () -> assertEquals(attributeRequest.type(), updatedAttribute.type())
        );

        deleteAttribute(existingAttribute.getId());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideInvalidAttributeRequests")
    public void testUpdateAttribute_whenAttributeRequestIsInvalid(AttributeRequest attributeRequest) {
        var existingAttribute = createAttribute("name", "type");

        mockMvc.perform(put(ATTRIBUTE_URI + "/" + existingAttribute.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );

        deleteAttribute(existingAttribute.getId());
    }

    @Test
    @SneakyThrows
    public void testUpdateAttribute_whenAttributeNotFound() {
        var attributeRequest = AttributeRequest.builder()
            .name("name")
            .type("type")
            .build();

        mockMvc.perform(put(ATTRIBUTE_URI + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }

    @Test
    @SneakyThrows
    public void testDeleteAttribute_whenAttributeExists_shouldDeleteAttributeById() {
        var existingAttribute = createAttribute("name", "type");

        deleteAttribute(existingAttribute.getId());

        mockMvc.perform(get(ATTRIBUTE_URI + "/" + existingAttribute.getId()))
            .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }

    @Test
    @SneakyThrows
    public void testDeleteAttribute_whenAttributeNotFound() {
        mockMvc.perform(delete(ATTRIBUTE_URI + "/1"))
            .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }

    private Attribute createAttribute(String name, String type) throws Exception {
        var attributeRequest = AttributeRequest.builder()
            .name(name)
            .type(type)
            .build();

        var mvcResponse = mockMvc.perform(post(ATTRIBUTE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse();

        return objectMapper.readValue(mvcResponse.getContentAsString(), Attribute.class);
    }

    private void deleteAttribute(Long id) throws Exception {
        mockMvc.perform(delete(ATTRIBUTE_URI + "/" + id))
            .andExpect(status().isNoContent());
    }
}
