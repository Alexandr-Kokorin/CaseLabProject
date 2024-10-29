package caselab.controller.document.facade.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Запрос на обновление документа")
public class UpdateDocumentRequest {
    @Schema(description = "Новое название документа",
            example = "Обновленное название")
    private String name = null;
}
