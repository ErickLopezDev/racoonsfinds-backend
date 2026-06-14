package com.racoonsfinds.backend.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Contenedor Postgres compartido (singleton) para los tests de integración.
 * Se arranca una sola vez por JVM y se reutiliza entre clases de test;
 * Ryuk lo limpia al terminar. Así los tests corren contra la misma base
 * que producción (Postgres real), no contra H2.
 */
public abstract class PostgresTestContainer {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
