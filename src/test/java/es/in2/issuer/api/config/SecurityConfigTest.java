package es.in2.issuer.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Mock
    private AppConfiguration appConfiguration;

    private SecurityConfig securityConfig;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(appConfiguration.getKeycloakDomain()).thenReturn("localhost:9090");
        securityConfig = new SecurityConfig(appConfiguration);
    }

    @Test
    public void testJwtDecoderLocal() {
        try (MockedStatic<ReactiveJwtDecoders> mocked = Mockito.mockStatic(ReactiveJwtDecoders.class)) {
            // Mock the static method call
            mocked.when(() -> ReactiveJwtDecoders.fromIssuerLocation(anyString()))
                    .thenReturn(mock(ReactiveJwtDecoder.class));

            // Execute the method under test
            ReactiveJwtDecoder jwtDecoder = securityConfig.jwtDecoderLocal();

            // Verify
            assertNotNull(jwtDecoder, "JwtDecoder should not be null for 'local' profile");
            // You can add more verifications if needed
        }
    }

    @Test
    public void testJwtDecoder() {
        // Test for the non-"local" profile
        ReactiveJwtDecoder jwtDecoder = securityConfig.jwtDecoder();
        assertNotNull(jwtDecoder, "JwtDecoder should not be null for non-'local' profiles");
    }

    @Test
    public void testCorsConfigurationSource() {
        var corsConfigurationSource = securityConfig.corsConfigurationSource();
        assertNotNull(corsConfigurationSource, "CorsConfigurationSource should not be null");
    }

    @Test
    public void testWebSecurityCustomizer() {
        var webSecurityCustomizer = securityConfig.webSecurityCustomizer();
        assertNotNull(webSecurityCustomizer, "WebSecurityCustomizer should not be null");
    }

    @Test
    public void testGetSwaggerPaths() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SecurityConfig securityConfig = new SecurityConfig(appConfiguration);

        // Access the private method
        Method method = SecurityConfig.class.getDeclaredMethod("getSwaggerPaths");
        method.setAccessible(true);

        String[] expected = new String[]{
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/api-docs/**",
                "/spring-ui/**",
                "/webjars/swagger-ui/**"
        };

        // Invoke the method and check the result
        String[] swaggerPaths = (String[]) method.invoke(securityConfig);
        //assertNotNull(swaggerPaths, "SwaggerPaths should not be null");
        assertEquals(Arrays.toString(swaggerPaths), Arrays.toString(expected), "Result should match expected");
    }
}