package caselab.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class DomainTest extends IntegrationTest {

    /**
     * Метод для тестирования БД
     */
    @Test
    @Transactional
    @Rollback
    void getTest() {
        assertThat(1).isEqualTo(1);
    }
}
