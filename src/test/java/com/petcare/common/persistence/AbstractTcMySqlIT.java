package com.petcare.common.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Abstract base class for Testcontainers MySQL 8 integration tests.
 * <p>
 * Starts a fixed MySQL 8 container once per test JVM and binds its
 * connection info to Spring's datasource via {@link DynamicPropertySource}.
 * <p>
 * Subclasses inherit {@code @SpringBootTest}, {@code @ActiveProfiles("tc-mysql")},
 * and the container lifecycle — they only need to declare their own test methods.
 */
@SpringBootTest
@ActiveProfiles("tc-mysql")
public abstract class AbstractTcMySqlIT {

    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0.46")
                    .withDatabaseName("petcare_o2o");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}
