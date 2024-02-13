package es.in2.issuer.api.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class LocalAppConfigServiceImplTest {
    @Mock
    private Environment env;
    @InjectMocks
    private LocalAppConfigServiceImpl configService;

    @Test
    void getConfiguration_Success() {
        String key = "exampleKey";
        String expectedValue = "exampleValue";
        when(env.containsProperty(key)).thenReturn(true);
        when(env.getProperty(key)).thenReturn(expectedValue);

        Mono<String> resultMono = configService.getConfiguration(key);
        String result = resultMono.block();
        assertNotNull(result);
        assertEquals(expectedValue, result);

        verify(env, times(1)).containsProperty(key);
        verify(env, times(2)).getProperty(key);
    }

    @Test
    void getConfiguration_PropertyNotFound() {
        String key = "nonExistentKey";
        when(env.containsProperty(key)).thenReturn(false);

        Exception exception = assertThrows(Exception.class, () -> configService.getConfiguration(key).block());
        assertEquals("java.lang.Exception: Property " + key + " not found", exception.getMessage());

        verify(env, times(1)).containsProperty(key);
    }

    @Test
    void getConfiguration_PropertyNull() {
        when(env.containsProperty(null)).thenReturn(true);

        Exception exception = assertThrows(Exception.class, () -> configService.getConfiguration(null).block());
        assertEquals("java.lang.Exception: Property " + null + " not found", exception.getMessage());

        verify(env, times(1)).containsProperty(null);
    }
}
