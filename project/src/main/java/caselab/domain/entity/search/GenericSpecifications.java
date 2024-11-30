package caselab.domain.entity.search;

import caselab.configuration.GenericFilterProperties;
import caselab.exception.search.FilterNotAllowedException;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class GenericSpecifications {

    private static List<String> allowedFilters;

    private final GenericFilterProperties genericFilterProperties;

    @PostConstruct
    public void init() {
        allowedFilters = genericFilterProperties.getFilters();
    }

    public static <T> Specification<T> filterBy(Map<String, List<Object>> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filters != null) {
                filters.forEach((fieldName, values) -> {
                    if (isFilterAllowed(fieldName)) {
                        if (values != null && !values.isEmpty()) {
                            Path<?> path = resolvePath(root, fieldName); // Рекурсивное разрешение пути
                            predicates.add(path.in(values));
                        }
                    } else {
                        throw new FilterNotAllowedException("filter.not.allowed", new Object[]{fieldName});
                    }
                });
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Path<?> resolvePath(Path<?> root, String fieldName) {
        String[] pathParts = fieldName.split("\\.");
        Path<?> currentPath = root;
        for (String part : pathParts) {
            if (currentPath instanceof Join) {
                currentPath = ((Join<?, ?>) currentPath).join(part);
            } else {
                currentPath = currentPath.get(part);
            }
        }
        return currentPath;
    }

    private static boolean isFilterAllowed(String filter) {
        return allowedFilters != null && allowedFilters.contains(filter);
    }
}
