package caselab.service.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageUtil {

    public static final int DEFAULT_PAGE_NUM = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;

    public static PageRequest toPageable(
        Integer pageNum,
        Integer pageSize,
        Sort sortBy,
        String sortStrategy
    ) {
        int num = pageNum != null ? pageNum : DEFAULT_PAGE_NUM;
        int size = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;

        Sort validSortBy = switch (sortStrategy.toLowerCase()) {
            case "desc" -> sortBy.descending();
            case "asc" -> sortBy.ascending();
            default -> throw new IllegalArgumentException("Parameter sortStrategy: " + sortStrategy + " is not valid");
        };

        return PageRequest.of(num, size, validSortBy);
    }

    public static PageRequest toPageable(
        Integer pageNum,
        Integer pageSize
    ) {
        int num = pageNum != null ? pageNum : DEFAULT_PAGE_NUM;
        int size = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;

        return PageRequest.of(num, size);
    }

}
