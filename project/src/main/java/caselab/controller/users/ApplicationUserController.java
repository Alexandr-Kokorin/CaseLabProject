package caselab.controller.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.service.users.ApplicationUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@RequestMapping("/api/v1/users")
public class ApplicationUserController {
    private final ApplicationUserService userService;

    @GetMapping
    @Operation(summary = "Получить информацию о всех пользователях",
               description = "Возвращает информацию о каждом пользователе")
    public ResponseEntity<List<UserResponse>> findAllUsers() {
        List<UserResponse> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить информацию о пользователе",
               description = "Возвращает информацию о пользователе по его идентификатору.")
    public ResponseEntity<UserResponse> findUserById(@PathVariable Long id) {
        UserResponse user = userService.findUser(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    @Operation(summary = "Обновить данные пользователя",
               description = "Обновляет данные пользователя по его идентификатору, "
                   + "если авторизованный пользователь — владелец аккаунта.")
    public ResponseEntity<UserResponse> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserUpdateRequest updateRequest
    ) {
        UserResponse updatedUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    @Operation(summary = "Удалить пользователя",
               description = "Удаляет пользователя по идентификатору, "
                   + "если авторизованный пользователь — владелец аккаунта.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
