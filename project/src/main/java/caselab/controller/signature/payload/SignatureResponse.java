package caselab.controller.signature.payload;

import caselab.domain.entity.enums.SignatureStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record SignatureResponse(
    @Schema(description = "ID подписи", example = "1")
    Long id,

    @Schema(description = "Название документа", example = "Договор о сотрудничестве")
    String name,

    @Schema(description = "Статус документа", example = "SIGNED")
    SignatureStatus status,

    @Schema(description = "Дата и время отправки документа на подпись", example = "2023-04-15T14:30:00+03:00")
    OffsetDateTime sentAt,

    @Schema(description = "Дата и время подписания документа", example = "2023-04-16T10:15:00+03:00")
    OffsetDateTime signedAt,

    @Schema(description = "Хеш подписи", example = "a1b2c3d4e5f6g7h8i9j0")
    String signatureData,

    @Schema(description = "Электронная почта пользователя, к которому относится подпись", example = "user@example.com")
    String email,

    @Schema(description = "ID документа для подписи", example = "1")
    Long documentId
) {
}
