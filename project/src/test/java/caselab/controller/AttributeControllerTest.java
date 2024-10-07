package caselab.controller;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.service.attribute.AttributeService;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AttributeControllerTest extends BaseControllerMockTest {
    private final String ATTRIBUTE_URI = "/api/v1/attributes";

    @MockBean
    private AttributeService attributeService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @Tag("Create")
    @Test
    public void testCreateAttribute_shouldReturnCreatedAttribute() throws Exception {
        AttributeRequest attributeRequest = new AttributeRequest("name", "type");
        AttributeResponse attributeResponse = new AttributeResponse(1L, "name", "type");

        Mockito.when(attributeService.createAttribute(any(AttributeRequest.class))).thenReturn(attributeResponse);

        mockMvc.perform(post(ATTRIBUTE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeRequest)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(attributeResponse.id()),
                jsonPath("$.name").value(attributeResponse.name()),
                jsonPath("$.type").value(attributeResponse.type())
            );
    }

    @Test
    public void testFindAttributeById_whenAttributeExists_shouldReturnAttributeById() throws Exception {
        AttributeResponse attributeResponse = new AttributeResponse(1L, "name", "type");

        Mockito.when(attributeService.findAttributeById(1L)).thenReturn(attributeResponse);

        mockMvc.perform(get(ATTRIBUTE_URI + "/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(attributeResponse.id()),
                jsonPath("$.name").value(attributeResponse.name()),
                jsonPath("$.type").value(attributeResponse.type())
            );
    }

    @Test
    public void testFindAttributeById_whenAttributeNotFound_shouldThrowNoSuchElementException() throws Exception {
        Mockito.when(attributeService.findAttributeById(2L)).thenThrow(new NoSuchElementException("Атрибут не найден"));

        mockMvc.perform(get(ATTRIBUTE_URI + "/2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testFindAllAttributes_shouldReturnListOfAttributes() throws Exception {
        AttributeResponse attributeResponse1 = new AttributeResponse(1L, "name1", "type1");
        AttributeResponse attributeResponse2 = new AttributeResponse(2L, "name2", "type2");

        List<AttributeResponse> attributeResponses = new ArrayList<>(List.of(attributeResponse1, attributeResponse2));

        Mockito.when(attributeService.findAllAttributes()).thenReturn(attributeResponses);

        mockMvc.perform(get(ATTRIBUTE_URI)
                .accept(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.[0].id").value(attributeResponse1.id()),
                jsonPath("$.[0].name").value(attributeResponse1.name()),
                jsonPath("$.[0].type").value(attributeResponse1.type()),
                jsonPath("$.[1].id").value(attributeResponse2.id()),
                jsonPath("$.[1].name").value(attributeResponse2.name()),
                jsonPath("$.[1].type").value(attributeResponse2.type())
            );
    }

    @Test
    public void testUpdateAttribute_whenAttributeExists_shouldReturnUpdatedAttribute() throws Exception {
        AttributeResponse attributeResponse = new AttributeResponse(1L, "newName", "newType");

        Mockito.when(attributeService.updateAttribute(eq(1L), any(AttributeRequest.class))).thenReturn(attributeResponse);

        mockMvc.perform(put(ATTRIBUTE_URI + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attributeResponse)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(attributeResponse.id()),
                jsonPath("$.name").value(attributeResponse.name()),
                jsonPath("$.type").value(attributeResponse.type())
            );
    }

    @Test
    public void testDeleteAttribute_whenAttributeExists_shouldDeleteAttributeById() throws Exception {
        Mockito.doNothing().when(attributeService).deleteAttribute(1L);

        mockMvc.perform(delete(ATTRIBUTE_URI + "/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteAttribute_whenAttributeNotFound_shouldThrowNoSuchElementException() throws Exception {
        Mockito.doThrow(new NoSuchElementException("Атрибут не найден")).when(attributeService).deleteAttribute(2L);

        mockMvc.perform(delete(ATTRIBUTE_URI + "/2"))
            .andExpect(status().isNotFound());
    }
}
