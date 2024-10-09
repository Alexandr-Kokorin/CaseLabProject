package caselab.controller.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.service.attribute.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attributes")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    @Operation(summary = "Добавить атрибут")
    @PostMapping
    public AttributeResponse createAttribute(@Valid @RequestBody AttributeRequest attributeRequest) {
        return attributeService.createAttribute(attributeRequest);
    }

    @Operation(summary = "Получить атрибут по id")
    @GetMapping("/{id}")
    public AttributeResponse findAttributeById(@PathVariable Long id) {
        return attributeService.findAttributeById(id);
    }

    @Operation(summary = "Получить список всех атрибутов")
    @GetMapping
    public List<AttributeResponse> findAllAttributes() {
        return attributeService.findAllAttributes();
    }

    @Operation(summary = "Изменить атрибут")
    @PutMapping("/{id}")
    public AttributeResponse updateAttribute(
        @PathVariable Long id,
        @Valid @RequestBody AttributeRequest attributeRequest
    ) {
        return attributeService.updateAttribute(id, attributeRequest);
    }

    @Operation(summary = "Удалить атрибут")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        attributeService.deleteAttribute(id);
        return ResponseEntity.noContent().build();
    }
}
