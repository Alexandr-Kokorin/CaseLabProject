package caselab.controller.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.service.users.ApplicationUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class ApplicationUserController {

    private final ApplicationUserService userService;

    @GetMapping("/all")
    @Operation(summary = "Получить информацию о всех пользователях",
               description = "Возвращает информацию о каждом пользователе")
    public List<UserResponse> findAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping
    @Operation(summary = "Получить информацию о пользователе",
               description = "Возвращает информацию о пользователе по его идентификатору.")
    public UserResponse findUser(@NotBlank String email) {
        return userService.findUser(email);
    }

    @PutMapping
    @Operation(summary = "Обновить данные пользователя",
               description = "Обновляет данные пользователя по его идентификатору, "
                   + "если авторизованный пользователь — владелец аккаунта.")
    public UserResponse updateUser(
        Authentication authentication,
        @Valid @RequestBody UserUpdateRequest updateRequest
    ) {
        return userService.updateUser(authentication, updateRequest);
    }

    @DeleteMapping
    @Operation(summary = "Удалить пользователя",
               description = "Удаляет пользователя по идентификатору, "
                   + "если авторизованный пользователь — владелец аккаунта.")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        userService.deleteUser(authentication);
        return ResponseEntity.noContent().build();
    }
}
