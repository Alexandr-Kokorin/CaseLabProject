package caselab.domain.repository.elastic;

import caselab.domain.elastic.AttributeDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeElasticRepository extends ElasticsearchRepository<AttributeDoc, String> {
    @Query("""
            {
              "bool": {
                "should": [
                  {
                    "match_phrase": {
                      "name": {
                        "query": "?0",
                        "boost": 2
                      }
                    }
                  }
                ],
                "minimum_should_match": 1
              }
            }
            """)
    Page<AttributeDoc> searchByQuery(String query, Pageable pageable);
}
