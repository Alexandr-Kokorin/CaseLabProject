package caselab.elastic.interfaces;

import org.springframework.data.domain.Page;

public interface ElasticSearchInterface<T> {
    Page<T> searchValuesElastic(String searchText, int page, int size);
}
