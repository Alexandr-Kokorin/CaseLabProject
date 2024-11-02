package caselab.controller.subscription;

import caselab.service.subscription.SubscriptionService;
import caselab.service.util.UserUtilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Подписки", description = "API для управления подписками на документы")
public class SubscriptionController {

    private final SubscriptionService subscribeService;
    private final UserUtilService userUtilService;

    @Operation(summary = "Получить все подписки",
               description = """
                   Возвращает массив id версий документов, на которые подписан текущий пользователь
                   """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение массива всех id версий документов"),
        @ApiResponse(responseCode = "404",
                     description = "Пользователь с указанным адресом электронной почты не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<List<Long>> getAllSubscriptions(Authentication authentication) {
        var user = userUtilService.findUserByAuthentication(authentication);
        var documentsVersionsIds = subscribeService.getIdsOfAllSubscribed(user.getEmail());

        return new ResponseEntity<>(documentsVersionsIds, HttpStatus.OK);
    }

    @Operation(summary = "Проверить подписку",
               description = """
                   Возвращает статус подписки текущего пользователя на указанную версию документа
                   """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная получение статуса подписки"),
        @ApiResponse(responseCode = "404", description = "Версия документа с указанным ID не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404",
                     description = "Пользователь с указанным адресом электронной почты не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<Map<String, Boolean>> isUserSubscribedToDocumentVersion(
        @Parameter(description = "ID версии документа", required = true)
        @RequestParam(name = "documentVersionId") Long documentVersionId,
        Authentication authentication
    ) {
        var user = userUtilService.findUserByAuthentication(authentication);
        boolean isSubscribed = subscribeService.isSubscribed(user.getEmail(), documentVersionId);

        return new ResponseEntity<>(Map.of("isSubscribed", isSubscribed), HttpStatus.OK);
    }

    @Operation(summary = "Подписаться",
               description = "Подписывает текущего пользователя на события версии документа с указанным ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная подписка на события документа"),
        @ApiResponse(responseCode = "404", description = "Версия документа с указанным ID не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404",
                     description = "Пользователь с указанным адресом электронной почты не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409",
                     description = "Текущий пользователь уже подписан на события этой версии документа",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<Void> subscribe(
        @Parameter(description = "ID версии документа", required = true)
        @RequestParam(name = "documentVersionId") Long documentVersionId,
        Authentication authentication
    ) {
        var user = userUtilService.findUserByAuthentication(authentication);
        subscribeService.subscribe(user.getEmail(), documentVersionId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отписаться",
               description = "Отписывает текущего пользователя от событий версии документа с указанным ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная подписка на события версии документа"),
        @ApiResponse(responseCode = "404", description = "Подписка не указанную версию документа не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь с указанным адресом электронной почты не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> unsubscribe(
        @Parameter(description = "ID версии документа", required = true)
        @RequestParam(name = "documentVersionId") Long documentVersionId,
        Authentication authentication
    ) {
        var user = userUtilService.findUserByAuthentication(authentication);
        subscribeService.unsubscribe(user.getEmail(), documentVersionId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отписаться от всех событий",
               description = """
                   Отписывает текущего пользователя от событий на все документы,
                   на которые им была совершена подписка
                   """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное удаление всех подписок пользователя"),
        @ApiResponse(responseCode = "404", description = "Пользователь с указанным адресом электронной почты не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/all")
    public ResponseEntity<Void> unsubscribe(Authentication authentication) {
        var user = userUtilService.findUserByAuthentication(authentication);
        subscribeService.unsubscribeAll(user.getEmail());

        return ResponseEntity.ok().build();
    }
}
