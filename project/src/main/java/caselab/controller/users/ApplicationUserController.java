package caselab.controller.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.service.users.ApplicationUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@RequestMapping("/api/v1/users")
public class ApplicationUserController {
    private final ApplicationUserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить информацию о всех пользователях",
               description = "Возвращает информацию о каждом пользователе")
    public List<UserResponse> findAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить информацию о пользователе",
               description = "Возвращает информацию о пользователе по его идентификатору.")
    public UserResponse findUserById(@PathVariable Long id) {
        return userService.findUser(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("#id == authentication.principal.id")
    @Operation(summary = "Обновить данные пользователя",
               description = "Обновляет данные пользователя по его идентификатору, "
                   + "если авторизованный пользователь — владелец аккаунта.")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest updateRequest) {
        return userService.updateUser(id, updateRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#id == authentication.principal.id")
    @Operation(summary = "Удалить пользователя",
               description = "Удаляет пользователя по идентификатору, "
                   + "если авторизованный пользователь — владелец аккаунта.")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
