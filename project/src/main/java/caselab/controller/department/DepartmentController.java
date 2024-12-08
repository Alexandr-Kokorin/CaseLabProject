package caselab.controller.department;

import caselab.controller.department.payload.AddEmployeeRequest;
import caselab.controller.department.payload.DeleteEmployeeRequest;
import caselab.controller.department.payload.DepartmentCreateRequest;
import caselab.controller.department.payload.DepartmentResponse;
import caselab.controller.department.payload.DepartmentUpdateRequest;
import caselab.controller.users.payload.UserResponse;
import caselab.service.department.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/departments")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Подразделения", description = "API взаимодействия с подразделениями")
public class DepartmentController {

    private final DepartmentService depService;

    @Operation(summary = "Создать подразделение",
               description = "Возвращает созданное подразделение, доступно только администратору")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное создание подразделения",
                     content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Конфликт при создании подразделения",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/")
    DepartmentResponse createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        return depService.createDepartment(request);
    }

    @Operation(summary = "Получить подразделение по id",
               description = "Возвращает подразделение по его id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Успешное получение подразделения",
                     content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Подразделение не найдено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    DepartmentResponse getDepartment(@PathVariable("id") Long id) {
        return depService.getDepartment(id);
    }

    @Operation(summary = "Отображение подразделений",
               description = "Возвращает структуру подразделений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение по индексу",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = DepartmentResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/all")
    public List<DepartmentResponse> getAllDepartments() {
        return depService.getAllDepartmentsHierarchy();
    }

    @Operation(summary = "Обновить данные подразделения",
               description = "Обновляет информацию о подразделении")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление информации о документе",
                     content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Подразделение не найдено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Конфликт при обновлении данных подразделения",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{id}")
    public DepartmentResponse updateDepartment(
        @PathVariable("id") Long id,
        @RequestBody DepartmentUpdateRequest request
    ) {
        return depService.updateDepartment(id, request);
    }

    @Operation(summary = "Удалить подразделение",
               description = "Удаляет информацию о подразделении")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление информации о документе",
                     content = @Content),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Подразделение не найдено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable("id") Long id) {
        depService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить работника в подразделение",
               description = "Добавляет работника в подразделение")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное добавление работника в подразделение",
                     content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Работник не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/add_employee")
    public UserResponse addEmployeeToDepartment(
        @PathVariable("id") Long id,
        @Valid @RequestBody AddEmployeeRequest addEmployeeRequest
    ) {
        return depService.addEmployeeToDepartment(id, addEmployeeRequest);
    }

    @Operation(summary = "Удалить работника из подразделения",
               description = "Удаляет работника из подразделения")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное удаление работника из подразделения",
                     content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Работник не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/delete_employee")
    public UserResponse deleteEmployeeFromDepartment(
        @Valid @RequestBody DeleteEmployeeRequest deleteEmployeeRequest
    ) {
        return depService.deleteEmployeeFromDepartment(deleteEmployeeRequest);
    }

    @Operation(summary = "Получить всех работников подразделения",
               description = "Возвращает всех работников подразделения")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение всех работников подразделения",
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Подразделение не найдено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}/employees")
    public List<UserResponse> getAllEmployeesForDepartment(
        @PathVariable("id") Long id
    ) {
        return depService.getAllEmployeesForDepartment(id);
    }
}
