package es.in2.issuer.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class WebClientConfigTest {
    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(WebClientConfig.class);

    @Test
    void testWebClientConfigBean() {
        context.run(it -> assertThat(it).hasSingleBean(WebClientConfig.class));
    }

}