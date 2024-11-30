package caselab.controller.users.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Ответ на запрос, содержащий информацию о пользователе")
public record UserResponse(
    @JsonProperty("email")
    @Schema(description = "Адрес электронной почты пользователя", example = "user@example.com")
    String email,
    @JsonProperty("display_name")
    @Schema(description = "Отображаемое имя пользователя", example = "Иван Иванов")
    String displayName,
    @Schema(description = "Должность пользователя", example = "Программист")
    @JsonProperty("position")
    String position,
    @Schema(description = "Работает ли пользователь (true/false)", example = "true")
    @JsonProperty("is_working")
    Boolean isWorking,
    @Schema(description = "id подразделения пользователя", example = "2")
    @JsonProperty("department_id")
    Long departmentId,
    @JsonProperty("roles")
    @Schema(description = "Роли пользователя", example = "[\"USER\"]")
    List<String> roles
) {
}
