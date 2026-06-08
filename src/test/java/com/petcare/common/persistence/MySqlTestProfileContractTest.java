package com.petcare.common.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlTestProfileContractTest {

    @Test
    void mysqlTestProfileUsesEnvironmentConfiguredMySql() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/application-mysql-test.yml")) {
            assertNotNull(input, "application-mysql-test.yml must exist for the phase 3 MySQL gate");

            String yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(yaml.contains("${DB_URL:"), "MySQL test URL must be configurable by DB_URL");
            assertTrue(yaml.contains("${DB_USERNAME:"), "MySQL test user must be configurable by DB_USERNAME");
            assertTrue(yaml.contains("${DB_PASSWORD:"), "MySQL test password must be configurable by DB_PASSWORD");
            assertTrue(yaml.contains("com.mysql.cj.jdbc.Driver"), "MySQL test profile must use the MySQL driver");
            assertTrue(!yaml.contains("jdbc:h2:"), "MySQL test profile must not fall back to H2");
        }
    }
}
