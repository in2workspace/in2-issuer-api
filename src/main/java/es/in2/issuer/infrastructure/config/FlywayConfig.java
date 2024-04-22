package es.in2.issuer.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    private final AppConfiguration appConfiguration;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(appConfiguration.getDbUrl())
                .username(appConfiguration.getDbUser())
                .password(appConfiguration.getDbPassword())
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    @Primary
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }
}
