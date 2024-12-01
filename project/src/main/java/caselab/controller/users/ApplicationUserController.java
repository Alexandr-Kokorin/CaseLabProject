package caselab.controller.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.search.SearchRequest;
import caselab.service.users.ApplicationUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API взаимодействия с пользователями")
public class ApplicationUserController {

    private final ApplicationUserService userService;

    @Operation(summary = "Получить список всех пользователей",
               description = "Возвращает список всех пользователей")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/all")
    public List<UserResponse> findAllUsers() {
        return userService.findAllUsers();
    }

    @Operation(summary = "Получить список пользователей по фильтрам",
               description = "Возвращает отфильтрованный список пользователей")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/all/advanced_search")
    public List<UserResponse> findAllUsersWithFilters(
        @NotNull(message = "{search.request.is_null}") @RequestBody SearchRequest searchRequest
    ) {
        return userService.findAllUsers(searchRequest);
    }

    @GetMapping("/current")
    @Operation(summary = "Получить информацию о текущем пользователе",
               description = "Возвращает информацию о текущем пользователе исходя из контекста аутентификации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public UserResponse getCurrentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }

    @GetMapping
    @Operation(summary = "Получить информацию о пользователе по его адресу электронной почты",
               description = "Возвращает информацию о пользователе по его адресу электронной почты")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь с указанным адресом электронной почты не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public UserResponse findUser(@RequestParam @NotBlank String email) {
        return userService.findUser(email);
    }

    @Operation(summary = "Обновить данные пользователя",
               description = "Обновляет данные о пользователе базе данных, "
                   + "если текущий пользователь владелец аккаунта")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление",
                     content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping
    public UserResponse updateUser(
        Authentication authentication,
        @Valid @RequestBody UserUpdateRequest updateRequest
    ) {
        return userService.updateUser(authentication, updateRequest);
    }

    @Operation(summary = "Удалить аккаунт пользователя",
               description = "Удаляет информацию о текущем пользователе из базы данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Пользователь с указанным адресом электронной почты не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Authentication authentication, @RequestParam String email) {
        userService.deleteUser(authentication, email);
        return ResponseEntity.noContent().build();
    }
}
