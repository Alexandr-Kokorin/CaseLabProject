package caselab.controller.signature.payload;

import caselab.domain.entity.enums.SignatureStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record SignatureResponse(
    @Schema(description = "ID подписи", example = "1")
    Long id,
    @Schema(description = "Навзание документа")
    String name,
    @Schema(description = "Статус документа")
    SignatureStatus status,
    @Schema(description = "Когда отправили документ на подпись")
    OffsetDateTime sentAt,
    @Schema(description = "Когда документ подписали")
    OffsetDateTime signedAt,
    @Schema(description = "Хеш подписи")
    String signatureData,
    @Schema(description = "Пользователь, к которому относится подпись")
    Long userId,
    @Schema(description = "Версия документа для подписи")
    Long documentVersionId
) {
}
