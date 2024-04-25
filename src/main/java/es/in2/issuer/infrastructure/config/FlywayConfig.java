package es.in2.issuer.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    private final AppConfiguration appConfiguration;
    @Bean
    public Flyway flyway() {
        Flyway flyway = Flyway.configure()
                .dataSource(appConfiguration.getDbUrl(),appConfiguration.getDbUser(),appConfiguration.getDbPassword())
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }
}
