package es.in2.issuer.backend.shared.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class WebClientConfigTest {

    private AppConfig appConfig;

    ApplicationContextRunner context;

    @BeforeEach
    void setUp() {
        // Mocking the VerifierConfig
        appConfig = Mockito.mock(AppConfig.class);
        Mockito.when(appConfig.getVerifierUrl()).thenReturn("https://mocked-domain.com");

        // Provide the WebClientConfig class and the mock VerifierConfig in the context
        context = new ApplicationContextRunner()
                .withBean(AppConfig.class, () -> appConfig)
                .withUserConfiguration(WebClientConfig.class);
    }

    @Test
    void testWebClientConfigBeans() {
        context.run(it -> {
            // Assert that the WebClientConfig class exists as a configuration
            assertThat(it).hasSingleBean(WebClientConfig.class);

            // Assert that both WebClient beans are present
            assertThat(it).hasBean("commonWebClient");
            assertThat(it).hasBean("oauth2VerifierWebClient");

            // Additionally, assert that each bean is of type WebClient
            assertThat(it.getBean("commonWebClient")).isInstanceOf(WebClient.class);
            assertThat(it.getBean("oauth2VerifierWebClient")).isInstanceOf(WebClient.class);
        });
    }
}