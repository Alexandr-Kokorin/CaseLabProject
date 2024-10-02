package caselab.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class DomainTest extends IntegrationTest {

    @Autowired
    private MyRepository repository;

    @Test
    @Transactional
    @Rollback
    void getTest() {
        var test = repository.existsById(1L);

        assertThat(test).isEqualTo(false);
    }
}
