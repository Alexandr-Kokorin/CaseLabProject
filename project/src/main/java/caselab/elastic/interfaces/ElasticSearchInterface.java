package caselab.elastic.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface ElasticSearchInterface<T> {
    Page<T> searchValuesElastic(String searchText, Integer page, Integer size, Authentication authentication);
}
