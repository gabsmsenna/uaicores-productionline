package dev.senna.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class ClientTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                // Configurações de teste para banco H2 em memória
                "quarkus.datasource.db-kind", "h2",
                "quarkus.datasource.username", "test",
                "quarkus.datasource.password", "test",
                "quarkus.datasource.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "quarkus.hibernate-orm.database.generation", "drop-and-create",
                "quarkus.hibernate-orm.log.sql", "true",

                // Configurações de log para testes
                "quarkus.log.level", "INFO",
                "quarkus.log.category.\"dev.senna\".level", "DEBUG"
        );
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }

}
