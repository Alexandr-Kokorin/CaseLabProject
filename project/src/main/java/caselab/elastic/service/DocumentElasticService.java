package caselab.elastic.service;

import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.entity.Document;
import caselab.domain.repository.DocumentRepository;
import caselab.elastic.interfaces.ElasticSearchInterface;
import caselab.elastic.repository.DocumentElasticRepository;
import caselab.service.document.mapper.DocumentMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import static java.util.Comparator.comparingInt;

@Service
@RequiredArgsConstructor
public class DocumentElasticService implements ElasticSearchInterface<DocumentResponse> {
    private final DocumentElasticRepository documentElasticRepository;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    public Page<DocumentResponse> searchValuesElastic(String searchText, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var searchResults = documentElasticRepository.searchByQuery(searchText, pageable);

        Map<Long, Integer> idsMap = new HashMap<>();

        var documentDocs = searchResults.getContent();

        for (int i = 0; i < documentDocs.size(); i++) {
            idsMap.put(documentDocs.get(i).getId(), i);
        }

        Set<Long> ids = idsMap.keySet();

        List<Document> documentsFromDb = documentRepository.findAllById(ids);
        documentsFromDb.sort(comparingInt(attribute -> idsMap.get(attribute.getId())));

        var result = documentsFromDb.stream()
            .map(documentMapper::entityToResponse)
            .toList();

        return new PageImpl<>(result, pageable, searchResults.getTotalElements());
    }
}
