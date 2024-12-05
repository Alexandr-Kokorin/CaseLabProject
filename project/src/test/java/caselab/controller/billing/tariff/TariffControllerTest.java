package caselab.controller.billing.tariff;

import caselab.controller.BaseControllerTest;
import caselab.controller.billing.tariff.payload.CreateTariffRequest;
import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.billing.tariff.payload.UpdateTariffRequest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class TariffControllerTest extends BaseControllerTest {

    private final String TARIFF_URI = "/api/v2/tariffs";

    private AuthenticationResponse adminToken;
    private AuthenticationResponse userToken;


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
                .header("X-TENANT-ID", "super-admin-tenant")
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
                .header("X-TENANT-ID", "tenant_1")
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
                .header("X-TENANT-ID", "super-admin-tenant")
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

        deleteRequest(createResponse.id(),token);
    }

    @Test
    @SneakyThrows
    @DisplayName("Обычный пользователь не может обновить тарифф")
    public void testUpdateTariffFail_UserUpdate() {
        var tokenAdmin = loginAdmin().accessToken();
        var tokenUser = loginUser().accessToken();
        var createTariffRequest = createTariffRequest();
        var mvcResponseCreate = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createTariffRequest),tokenAdmin);
        var createResponse = readValue(mvcResponseCreate, TariffResponse.class);
        var updateTariffRequest = UpdateTariffRequest.builder()
            .name("name new")
            .tariffDetails("details new")
            .price(2.)
            .userCount(2)
            .build();

        var mvcResponseUpdate = mockMvc.perform(put(TARIFF_URI+"/"+createResponse.id())
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1")
                .content(objectMapper.writeValueAsString(updateTariffRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andReturn();

        deleteRequest(createResponse.id(),tokenAdmin);
    }
    @Test
    @SneakyThrows
    @DisplayName("Админ безуспешно обновляет тариф без поля")
    public void testUpdateTariffFail_AdminUpdate(){
        var tokenAdmin = loginAdmin().accessToken();
        var createTariffRequest = createTariffRequest();
        var mvcResponse = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createTariffRequest),tokenAdmin);

        var response = readValue(mvcResponse, TariffResponse.class);

        var tariffUpdateRequest = UpdateTariffRequest.builder()
            .tariffDetails("details new")
            .price(2.)
            .userCount(2)
            .build();

        var updateMvc = mockMvc.perform(put(TARIFF_URI+"/"+response.id())
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("X-TENANT-ID", "super-admin-tenant")
                .content(objectMapper.writeValueAsString(tariffUpdateRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();

        deleteRequest(response.id(),tokenAdmin);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тест загружает список тарифов")
    public void getAllTariffsSuccess() {
        List<TariffResponse> tariffResponses = createTariffsForGetAll();
        var adminToken = loginAdmin().accessToken();

        var mvcResponse = mockMvc.perform(get(TARIFF_URI)
                .header("Authorization", "Bearer " + adminToken)
                .header("X-TENANT-ID", "super-admin-tenant")
            .param("pageNum", "0")
            .param("pageSize", "10")
            .param("sortStrategy", "ASC"))
            .andExpect(status().isOk())
            .andReturn();


        var responseMap = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            new TypeReference<Map<String, Object>>() {}
        );
        var tariffsContent = objectMapper.convertValue(
            responseMap.get("content"),
            new TypeReference<List<TariffResponse>>() {}
        );

        assertAll(
            () -> assertThat(tariffsContent).hasSize(7)
        );

        tariffResponses.forEach(tariff -> deleteRequest(tariff.id(), adminToken));
    }

    @Test
    @SneakyThrows
    @DisplayName("Админ удаляет тарифф успешно")
    public void testDeleteTariffSuccess_AdminDelete() {
        var adminToken = loginAdmin().accessToken();
        var createTariffRequest = createTariffRequest();
        var mvcResponse = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createTariffRequest),adminToken);
        var response = readValue(mvcResponse, TariffResponse.class);

        var mvcDelete = mockMvc.perform(delete(TARIFF_URI + "/" + response.id())
                .header("Authorization", "Bearer " + adminToken)
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @DisplayName("Пользователь не может удалить тарифф")
    public void testDeleteTariffFail_UserDelete() {
        var adminToken = loginAdmin().accessToken();
        var userToken = loginUser().accessToken();

        var createTariffRequest = createTariffRequest();
        var mvcResponse = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createTariffRequest),adminToken);
        var response = readValue(mvcResponse, TariffResponse.class);

        var mvcDelete = mockMvc.perform(delete(TARIFF_URI + "/" + response.id())
                .header("Authorization", "Bearer " + userToken)
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(status().isForbidden());

        deleteRequest(response.id(), adminToken);
    }
    @Test
    @SneakyThrows
    @DisplayName("Админ безуспешно удаляет тариф по неверному id")
    public void testDeleteTariffFail_AdminDelete() {
        var adminToken = loginAdmin().accessToken();
        var createRequest = createTariffRequest();
        var mvcResponse = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createRequest),adminToken);
        var response = readValue(mvcResponse, TariffResponse.class);

        var mvcDelete = mockMvc.perform(delete(TARIFF_URI + "/" + response.id()+1)
                .header("Authorization", "Bearer " + adminToken)
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isNotFound());

        deleteRequest(response.id(), adminToken);
    }

    @Test
    @SneakyThrows
    @DisplayName("Поиск тарифа по id")
    public void findTariffByIdSuccess() {
        var userToken = loginUser().accessToken();
        var adminToken = loginAdmin().accessToken();

        var createTariffRequest = createTariffRequest();
        var mvcResponse = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createTariffRequest),adminToken);
        var response = readValue(mvcResponse, TariffResponse.class);

        var findMvcResponse = mockMvc.perform(get(TARIFF_URI + "/" + response.id())
                .header("Authorization", "Bearer " + userToken)
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(status().isOk())
            .andReturn();
        var findResponse = readValue(findMvcResponse, TariffResponse.class);
        assertAll(
            () -> assertThat(findResponse.name()).isEqualTo(response.name()),
            () -> assertThat(findResponse.tariffDetails()).isEqualTo(response.tariffDetails()),
            () -> assertThat(findResponse.id()).isEqualTo(response.id()),
            () -> assertThat(findResponse.price()).isEqualTo(response.price()),
            () -> assertThat(findResponse.userCount()).isEqualTo(response.userCount())
        );
        deleteRequest(response.id(), adminToken);
    }

    @Test
    @SneakyThrows
    @DisplayName("Не нашли тариф по айди")
    public void testFindTariffByIdFail() {
        var userToken = loginUser().accessToken();
        var adminToken = loginAdmin().accessToken();

        var createTariffRequest = createTariffRequest();
        var mvcResponse = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createTariffRequest),adminToken);
        var response = readValue(mvcResponse, TariffResponse.class);

        var findMvcResponse = mockMvc.perform(get(TARIFF_URI + "/" + response.id()+1)
                .header("Authorization", "Bearer " + userToken)
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(status().isNotFound())
            .andReturn();

        deleteRequest(response.id(), adminToken);
    }

    @SneakyThrows
    private void deleteRequest(Long id, String token) {
        mockMvc.perform(delete(TARIFF_URI + "/" + id)
                .header("Authorization", "Bearer " + token)
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isNoContent());
    }
    @SneakyThrows
    private MvcResult createRequest(String url, String request, String token) {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-TENANT-ID", "super-admin-tenant"))
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
        if (adminToken != null) {
            return adminToken;
        }

        var request = AuthenticationRequest.builder()
            .email("superadmin@gmail.com")
            .password("admin321@&123")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(
                status().isOk()
            )
            .andReturn();

        adminToken = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return adminToken;
    }

    @SneakyThrows
    private AuthenticationResponse loginUser() {
        if (userToken != null) {
            return userToken;
        }

        var request = AuthenticationRequest.builder()
            .email("user@example.com")
            .password("password")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(
                status().isOk()
            )
            .andReturn();

        userToken = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return userToken;
    }


    @SneakyThrows
    private List<TariffResponse> createTariffsForGetAll(){
        var adminToken = loginAdmin().accessToken();
        var createRequest = createTariffRequest();
        var createRequest2 = CreateTariffRequest.builder()
            .name("name2")
            .tariffDetails("details2")
            .price(2.)
            .userCount(2)
            .build();

        var mvcResponse1 = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createRequest),adminToken);
        var mvcResponse2 = createRequest(TARIFF_URI,objectMapper.writeValueAsString(createRequest2),adminToken);
        var response1 = readValue(mvcResponse1, TariffResponse.class);
        var response2 = readValue(mvcResponse2, TariffResponse.class);

        return List.of(response1, response2);
    }
}
