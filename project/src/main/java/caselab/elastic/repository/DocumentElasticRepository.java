package caselab.elastic.repository;

import caselab.elastic.entity.AttributeDoc;
import caselab.elastic.entity.DocumentDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentElasticRepository extends ElasticsearchRepository<DocumentDoc, String> {
    @Query("""
       {
         "bool": {
           "should": [
             {
               "fuzzy": {
                 "name": {
                   "value": "?0",
                   "fuzziness": 2
                 }
               }
             }
           ]
         }
       }
       """)
    Page<AttributeDoc> searchByQuery(String query, Pageable pageable);
}
