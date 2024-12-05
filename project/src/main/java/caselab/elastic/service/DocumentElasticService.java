package caselab.elastic.service;

import caselab.controller.document.facade.payload.DocumentFacadeResponse;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.elastic.interfaces.ElasticSearchInterface;
import caselab.elastic.repository.DocumentElasticRepository;
import caselab.service.document.facade.DocumentFacadeService;
import caselab.service.util.PageUtil;
import caselab.service.util.UserUtilService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentElasticService implements ElasticSearchInterface<DocumentFacadeResponse> {
    private final DocumentElasticRepository documentElasticRepository;
    private final DocumentFacadeService documentFacadeService;
    private final UserUtilService utilService;

    @Override
    public Page<DocumentFacadeResponse> searchValuesElastic(
        String searchText,
        Integer page,
        Integer size,
        Authentication authentication
    ) {
        var pageable = PageUtil.toPageable(page, size);
        var searchResults = documentElasticRepository.searchByQuery(searchText, pageable);

        Map<Long, Integer> idsMap = new HashMap<>();

        var documentDocs = searchResults.getContent();

        IntStream.range(0, documentDocs.size())
            .forEach(i -> idsMap.put(documentDocs.get(i).getId(), i));

        Set<Long> ids = idsMap.keySet();

        var user = utilService.findUserByAuthentication(authentication);

        var permissions = user.getGlobalPermissions();

        var isAdmin = permissions.stream().anyMatch(per -> per.getName().equals(GlobalPermissionName.ADMIN));

        if (!isAdmin) {
            var userDocumentsIds = utilService.findUserByAuthentication(authentication).getUsersToDocuments().stream()
                .filter(usd -> ids.contains(usd.getDocument().getId()))
                .toList().stream()
                .map(UserToDocument::getId).toList();
            return documentFacadeService.getAllDocumentsByIds(userDocumentsIds, pageable);
        }

        return documentFacadeService.getAllDocumentsByIds(ids.stream().toList(), pageable);
    }

}
