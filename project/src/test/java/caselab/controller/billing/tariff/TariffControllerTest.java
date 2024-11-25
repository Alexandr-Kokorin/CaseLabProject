package caselab.controller.billing.tariff;

import caselab.controller.BaseControllerTest;
import caselab.controller.billing.tariff.payload.CreateTariffRequest;
import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.billing.tariff.payload.UpdateTariffRequest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TariffControllerTest extends BaseControllerTest {

    private final String TARIFF_URI = "/api/v2/tariffs";

    private AuthenticationResponse token;


    @Test
    @SneakyThrows
    @DisplayName("Админ успешно создает тариф")
    public void testCreateTariffSuccess() {
        var token = loginAdmin().accessToken();

        var tariffCreateRequest = createTariffRequest();
        var mvcResponse = createRequest(TARIFF_URI, objectMapper.writeValueAsString(tariffCreateRequest), token);
        var response = readValue(mvcResponse, TariffResponse.class);

        assertAll(
            "Проверка успешного создания типа документа админом",
            () -> assertThat(response.name()).isEqualTo("name"),
            () -> assertThat(response.tariffDetails()).isEqualTo("details"),
            () -> assertThat(response.price()).isEqualTo(1.0),
            () -> assertThat(response.userCount()).isEqualTo(1)
        );

        deleteRequest(response.id(),token);
    }

    @Test
    @SneakyThrows
    @DisplayName("Админ создает тариф без указания названия")
    public void testCreateTariffFail() {
        var token = loginAdmin().accessToken();
        var request = CreateTariffRequest.builder()
            .price(1.)
            .userCount(1)
            .tariffDetails("details")
            .build();
        var mvcResponse = mockMvc.perform(post(TARIFF_URI)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();
    }
    @Test
    @SneakyThrows
    @DisplayName("Пользователь не может создать тарифф")
    public void testCreateTariffFail_userCreate() {
        var token = loginUser().accessToken();
        var request = createTariffRequest();

        var mvcResponse = mockMvc.perform(post(TARIFF_URI)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andReturn();
    }
    @Test
    @SneakyThrows
    @DisplayName("Админ обновляет тариф")
    public void testUpdateTariffSuccess() {
        var token = loginAdmin().accessToken();
        var createTariffRequest = createTariffRequest();
        var mvcResponseCreate = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createTariffRequest),token);

        var createResponse = readValue(mvcResponseCreate, TariffResponse.class);

        var updateTariffRequest = UpdateTariffRequest.builder()
            .name("name new")
            .tariffDetails("details new")
            .price(2.)
            .userCount(2)
            .build();

        var mvcResponseUpdate = mockMvc.perform(put(TARIFF_URI+"/"+createResponse.id())
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(updateTariffRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        var updateResponse = readValue(mvcResponseUpdate, TariffResponse.class);
        assertAll(
            () -> assertThat(updateResponse.name()).isEqualTo("name new"),
            () -> assertThat(updateResponse.tariffDetails()).isEqualTo("details new"),
            () -> assertThat(updateResponse.price()).isEqualTo(2.),
            () -> assertThat(updateResponse.userCount()).isEqualTo(2)
        );
    }

    @SneakyThrows
    private void deleteRequest(Long id, String token) {
        mockMvc.perform(delete(TARIFF_URI + "/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }
    @SneakyThrows
    private MvcResult createRequest(String url, String request, String token) {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }
    @SneakyThrows
    private <T> T readValue(MvcResult mvcResponse, Class<T> valueType) {
        return objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            valueType
        );
    }

    private CreateTariffRequest createTariffRequest(){
        return CreateTariffRequest.builder()
            .name("name")
            .tariffDetails("details")
            .price(1.0)
            .userCount(1)
            .build();
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
}
