package caselab.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class ServiceTest {

    @Test
    void getTest() {
        assertThat(1).isEqualTo(1);
    }
}
