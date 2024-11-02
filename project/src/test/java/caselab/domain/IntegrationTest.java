package caselab.domain;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class IntegrationTest{
    public static PostgreSQLContainer<?> POSTGRES;
    private static final Integer CONTAINER_STARTUP_TIMEOUT_MINUTES = 10;
    private static final ElasticsearchContainer ELASTIC_CONTAINER;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("project")
            .withUsername("postgres")
            .withPassword("postgres");
        POSTGRES.start();

        ELASTIC_CONTAINER =
            new ElasticsearchContainer(DockerImageName.parse("elasticsearch")
                .withTag("8.15.2"))
                .withStartupTimeout(Duration.of(CONTAINER_STARTUP_TIMEOUT_MINUTES, ChronoUnit.MINUTES))
                .withSharedMemorySize(256000000L)
                .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m")
                .withEnv("xpack.security.enabled", "false")
                .withReuse(true);
        ELASTIC_CONTAINER.start();

        try {
            runMigrations(POSTGRES);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void runMigrations(JdbcDatabaseContainer<?> c) throws Exception {
        Path path = new File(".").toPath().toAbsolutePath().getParent().getParent().resolve("migrations/db/changelog/");

        Connection connection = DriverManager.getConnection(c.getJdbcUrl(), c.getUsername(), c.getPassword());
        Database database =
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

        Liquibase liquibase = new Liquibase("db.changelog-test.yml", new DirectoryResourceAccessor(path), database);

        liquibase.update(new Contexts(), new LabelExpression());
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.elasticsearch.uris", ELASTIC_CONTAINER::getHttpHostAddress);
    }
}
