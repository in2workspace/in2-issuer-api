//package es.in2.issuer.infrastructure.config;
//
//import org.flywaydb.core.Flyway;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
//import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.when;
//
//public class FlywayConfigTest {
//
//    @Mock
//    private FlywayProperties flywayProperties;
//
//    @Mock
//    private R2dbcProperties r2dbcProperties;
//
//    private FlywayConfig flywayConfig;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        flywayConfig = new FlywayConfig();
//    }
//
//    @Test
//    void testFlywayBean() {
//        // Setup mock properties
//        when(flywayProperties.getUrl()).thenReturn("jdbc:h2:mem:testdb");
//        when(r2dbcProperties.getUsername()).thenReturn("sa");
//        when(r2dbcProperties.getPassword()).thenReturn("");
//        when(flywayProperties.getLocations()).thenReturn(List.of("db/migration"));
//
//        // Create Flyway bean
//        Flyway flyway = flywayConfig.flyway(flywayProperties, r2dbcProperties);
//
//        // Verify that the Flyway bean is not null
//        assertNotNull(flyway);
//    }
//}