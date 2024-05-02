package es.in2.issuer.infrastructure.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
@EnableR2dbcRepositories(basePackages = "es.in2.issuer.domain.repository")
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    private final AppConfiguration appConfiguration;

    public DatabaseConfig(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @NotNull
    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "postgresql")
                .option(HOST, "localhost")
                .option(PORT, 5432) // Optional if default
                .option(USER, appConfiguration.getDbUser())
                .option(PASSWORD, appConfiguration.getDbPassword())
                .option(DATABASE, "issuer")
                .build());
    }
}