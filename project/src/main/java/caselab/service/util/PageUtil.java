package caselab.service.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageUtil {

    public static PageRequest toPageable(
        Integer pageNum,
        Integer pageSize,
        Sort sortBy,
        String sortStrategy
    ) {
        pageNum = pageNum != null ? pageNum : 0;
        pageSize = pageSize != null ? pageSize : 10;
        sortStrategy = sortStrategy != null ? sortStrategy : "desc";

        sortBy = switch (sortStrategy.toLowerCase()) {
            case "desc" -> sortBy.descending();
            case "asc" -> sortBy.ascending();
            default -> throw new IllegalArgumentException("Parameter sortStrategy: " + sortStrategy + " is not valid");
        };

        return PageRequest.of(pageNum, pageSize, sortBy);
    }

    public static PageRequest toPageable(
        Integer pageNum,
        Integer pageSize
    ) {
        pageNum = pageNum != null ? pageNum : 0;
        pageSize = pageSize != null ? pageSize : 10;

        return PageRequest.of(pageNum, pageSize);
    }

}
