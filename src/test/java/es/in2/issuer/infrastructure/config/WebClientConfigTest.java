package es.in2.issuer.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class WebClientConfigTest {

    ApplicationContextRunner context;

    @BeforeEach
    void setUp() {
        // Provide the WebClientConfig class and the mock VerifierConfig in the context
        context = new ApplicationContextRunner()
                .withUserConfiguration(WebClientConfig.class);
    }

    @Test
    void testWebClientConfigBeans() {
        context.run(it -> {
            // Assert that the WebClientConfig class exists as a configuration
            assertThat(it).hasSingleBean(WebClientConfig.class);

            // Assert that both WebClient beans are present
            assertThat(it).hasBean("commonWebClient");

            // Additionally, assert that each bean is of type WebClient
            assertThat(it.getBean("commonWebClient")).isInstanceOf(WebClient.class);
        });
    }
}