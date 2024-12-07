package caselab.controller.billing.bill;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BillControllerTest extends BaseControllerTest {

    private AuthenticationResponse adminToken;
    private AuthenticationResponse superAdminToken;

    @Test
    @DisplayName("Получение счета по ID - Успех")
    @SneakyThrows
    void testGetBillById() {
        Long billId = 1L;
        var token = loginAdmin().accessToken();

        mockMvc.perform(get("/api/v2/billings/" + billId)
                .header("Authorization", "Bearer " + token)
                .header("X-TENANT-ID", "tenant_1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(billId))
            .andExpect(jsonPath("$.email").value("admin@gmail.com"))
            .andExpect(jsonPath("$.organization.name").value("Organization A"));
    }

    @Test
    @DisplayName("Получение счета по ID - Ошибка доступ к ресурам разрешен только с ролью Admin, а не SUPER_ADMIN")
    @SneakyThrows
    void testGetBillById_FailRole() {
        Long billId = 1L;

        mockMvc.perform(get("/api/v2/billings/" + billId)
                .header("Authorization", "Bearer " + loginSuperAdmin().accessToken())
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Получение счета по ID - Ошибка не совпадение организации счета и пользователя кто делает запрос")
    @SneakyThrows
    void testGetBillById_FailMatch() {
        Long billId = 1L;

        mockMvc.perform(get("/api/v2/billings/" + billId)
                .header("Authorization", "Bearer " + loginAdmin2().accessToken())
                .header("X-TENANT-ID", "tenant_2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Блокировка организации - Успех")
    @SneakyThrows
    void testBlockOrganization_Success() {
        Long organizationId = 1L;

        mockMvc.perform(post("/api/v2/billings/block-organization/" + organizationId)
                .header("Authorization", "Bearer " +loginSuperAdmin().accessToken())
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Блокировка организации - Ошибка доступа с ролью ADMIN")
    @SneakyThrows
    void testBlockOrganization_FailRole() {
        Long organizationId = 1L;

        mockMvc.perform(post("/api/v2/billings/block-organization/" + organizationId)
                .header("Authorization", "Bearer " + loginAdmin().accessToken())
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Блокировка организации - Ошибка: организация не найдена")
    @SneakyThrows
    void testBlockOrganization_FailNotFound() {
        Long nonExistentOrganizationId = 999L;

        mockMvc.perform(post("/api/v2/billings/block-organization/" + nonExistentOrganizationId)
                .header("Authorization", "Bearer " + loginSuperAdmin().accessToken())
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Активация организации - Успех")
    @SneakyThrows
    void testSetOrganizationActive_Success(){
        Long organizationId = 1L;

        mockMvc.perform(post("/api/v2/billings/payment-success/" + organizationId)
                .header("Authorization", "Bearer " + loginSuperAdmin().accessToken())
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Активация организации - Ошибка доступа с ролью ADMIN")
    void testSetOrganizationActive_FailRole() throws Exception {
        Long organizationId = 1L;

        mockMvc.perform(post("/api/v2/billings/payment-success/" + organizationId)
                .header("Authorization", "Bearer " + loginAdmin().accessToken())
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Активация организации - Ошибка: организация не найдена")
    void testSetOrganizationActive_FailNotFound() throws Exception {
        Long nonExistentOrganizationId = 999L;

        mockMvc.perform(post("/api/v2/billings/payment-success/" + nonExistentOrganizationId)
                .header("Authorization", "Bearer " + loginSuperAdmin().accessToken())
                .header("X-TENANT-ID", "super-admin-tenant"))
            .andExpect(status().isNotFound());
    }



    @SneakyThrows
    private AuthenticationResponse loginAdmin() {
        if (adminToken != null) {
            return adminToken;
        }

        var request = AuthenticationRequest.builder()
            .email("admin@gmail.com")
            .password("admin321@&123")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-TENANT-ID", "tenant_1"))
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
    private AuthenticationResponse loginAdmin2() {
        if (adminToken != null) {
            return adminToken;
        }

        var request = AuthenticationRequest.builder()
            .email("admin2@gmail.com")
            .password("admin321@&123")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-TENANT-ID", "tenant_2"))
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
    private AuthenticationResponse loginAsOrganizationAdmin(String email, String password) {
        var request = AuthenticationRequest.builder()
            .email(email)
            .password(password)
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );
    }
    @SneakyThrows
    private AuthenticationResponse loginSuperAdmin() {
        if (superAdminToken != null) {
            return superAdminToken;
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

        superAdminToken = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return superAdminToken;
    }
}
