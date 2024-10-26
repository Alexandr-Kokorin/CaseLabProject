package caselab.domain;

import caselab.elastic.repository.DocumentElasticRepository;
import org.springframework.boot.test.mock.mockito.MockBean;

public class DocumentElasticTest {
    @MockBean
    private DocumentElasticRepository repository;
}
