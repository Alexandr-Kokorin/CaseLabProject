package caselab.domain.entity.search;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = """
        DTO для передачи фильтров поиска.

        <h3>Доступные фильтры:</h3>
        <h4>Фильтрация по документам:</h4>
        <ul>
          <li><b>id</b> — уникальный идентификатор документа</li>
          <li><b>name</b> — название документа</li>
          <li><b>documentType.id</b> — идентификатор типа документа</li>
          <li><b>documentType.name</b> — название типа документа</li>
          <li><b>status</b> — статус документа</li>
        </ul>
        <h4>Фильтрация по пользователям:</h4>
        <ul>
          <li><b>id</b> — уникальный идентификатор пользователя</li>
          <li><b>email</b> — email пользователя</li>
          <li><b>displayName</b> — отображаемое имя пользователя</li>
        </ul>
        <h4>Фильтрация по типам документов:</h4>
        <ul>
          <li><b>id</b> — уникальный идентификатор типа документа</li>
          <li><b>name</b> — название типа документа</li>
          <li><b>documentTypesToAttributes.isOptional</b> — флаг, указывающий, является ли атрибут необязательным</li>
          <li><b>documentTypesToAttributes.attribute.id</b> — идентификатор атрибута</li>
          <li><b>documentTypesToAttributes.attribute.name</b> — название атрибута</li>
          <li><b>documentTypesToAttributes.attribute.type</b> — тип атрибута</li>
        </ul>
        """
)
public class SearchRequest {

    @Schema(
        description = """
            Фильтры для поиска, где ключ — это название поля, а значение — список значений для фильтрации.

            Доступные фильтры указаны в описании класса.
            """,
        example = "{\"name\": [\"Документ1\", \"Документ2\"], \"documentType.id\": [1, 2]}"
    )
    private Map<String, List<Object>> filters;

    public void addFilter(String key, List<Object> values) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        if (!filters.containsKey(key)) {
            filters.put(key, new ArrayList<>());
        }
        filters.get(key).addAll(values);
    }
}
