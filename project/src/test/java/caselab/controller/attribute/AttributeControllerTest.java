package caselab.controller.attribute;

import caselab.controller.BaseControllerTest;
import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.domain.entity.Attribute;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
    private static AuthenticationResponse token;


    public static Stream<Arguments> provideInvalidAttributeRequests() {
        return invalidAttributeRequests.get();
    }

    @SneakyThrows
    private AuthenticationResponse loginAdmin() {
        if (token != null) {
            return token;
        }

        var request = AuthenticationRequest.builder()
            .email("admin@gmail.com")
            .password("admin321@&123")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn();

        token = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return token;
    }
    @SneakyThrows
    private AuthenticationResponse loginUser() {
        if (token != null) {
            return token;
        }

        var request = AuthenticationRequest.builder()
            .email("user@example.com")
            .password("password")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn();

        token = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return token;
    }


    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    public void testCreateAttribute_whenAttributeRequestIsValid_shouldReturnCreatedAttribute_forAdmin() {
        var token = loginAdmin().accessToken();
        var attributeRequest = AttributeRequest.builder()
            .name("name")
            .type("type")
            .build();

        var mvcResponse = mockMvc.perform(post(ATTRIBUTE_URI)
                .header("Authorization", "Bearer " + token) // Вставка токена в заголовок
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
        var token = loginAdmin().accessToken();

        mockMvc.perform(post(ATTRIBUTE_URI)
                .header("Authorization", "Bearer " + token) // Вставка токена в заголовок
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }
    // Тут написал админа в тесте, потому что используется создание атрибута, доступ к которому
    //есть только у админа
    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    public void testFindAttributeById_whenAttributeExists_shouldReturnAttributeById() {
        var token = loginUser().accessToken();
        var existingAttribute = createAttribute("name", "type");

        var mvcResponse = mockMvc.perform(get(ATTRIBUTE_URI + "/" + existingAttribute.getId())
                .header("Authorization", "Bearer " + token) // Вставка токена в заголовок
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
    @WithMockUser(username = "user@exmaple.com", roles = {"USER"})
    public void testFindAttributeById_whenAttributeNotFound() {
        var token = loginUser().accessToken();
        mockMvc.perform(get(ATTRIBUTE_URI + "/" + 1)
                .header("Authorization", "Bearer " + token) // Вставка токена в заголовок
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            )
            .andReturn()
            .getResponse();
    }

    // TODO - что-нибудь сделать, сейчас не работает
    /*
    //Тут так же использую админа, потому что есть создание аттрибута
    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    public void testFindAllAttributes_whenAtLeastOneAttributeExists_shouldReturnListOfAttributes() {
        var token = loginAdmin().token();

        var existingAttribute = createAttribute("name", "type");

        var mvcResponse = mockMvc.perform(get(ATTRIBUTE_URI)
                .header("Authorization", "Bearer " + token)
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
    @WithMockUser(username = "user@exmaple.com", roles = {"USER"})
    public void testFindAllAttributes_whenThereIsNoAttribute_shouldReturnEmptyList() {
        var token = loginUser().token();
        var mvcResponse = mockMvc.perform(get(ATTRIBUTE_URI)
                .header("Authorization", "Bearer " + token)
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
     */

    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    public void testUpdateAttribute_whenAttributeExists_shouldReturnUpdatedAttribute() {
        var token = loginAdmin().accessToken();
        var existingAttribute = createAttribute("name", "type");

        var attributeRequest = AttributeRequest.builder()
            .name("newName")
            .type("newType")
            .build();

        var mvcResponse = mockMvc.perform(put(ATTRIBUTE_URI + "/" + existingAttribute.getId())
                .header("Authorization", "Bearer " + token)
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
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    public void testUpdateAttribute_whenAttributeRequestIsInvalid(AttributeRequest attributeRequest) {
        var token = loginAdmin().accessToken();
        var existingAttribute = createAttribute("name", "type");

        mockMvc.perform(put(ATTRIBUTE_URI + "/" + existingAttribute.getId())
                .header("Authorization", "Bearer " + token)
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
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    public void testUpdateAttribute_whenAttributeNotFound() {
        var token = loginAdmin().accessToken();
        var attributeRequest = AttributeRequest.builder()
            .name("name")
            .type("type")
            .build();

        mockMvc.perform(put(ATTRIBUTE_URI + "/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    public void testDeleteAttribute_whenAttributeNotFound() {
        var token = loginAdmin().accessToken();
        mockMvc.perform(delete(ATTRIBUTE_URI + "/1"))
            .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            );
    }


    @WithMockUser(username = "admin@gmail.com", roles = {"ADMIN"})
    private Attribute createAttribute(String name, String type) throws Exception {
        var token = loginAdmin().accessToken();
        var attributeRequest = AttributeRequest.builder()
            .name(name)
            .type(type)
            .build();

        var mvcResponse = mockMvc.perform(post(ATTRIBUTE_URI)
                .header("Authorization", "Bearer " + token) // Вставка токена в заголовок
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
