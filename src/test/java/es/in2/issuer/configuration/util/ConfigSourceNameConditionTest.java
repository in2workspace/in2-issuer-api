package es.in2.issuer.configuration.util;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigSourceNameConditionTest {
    @Test
    public void testMatchesWhenPropertyMatchesAnnotation() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.config-source.name")).thenReturn("value");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        when(metadata.getAnnotationAttributes(ConfigSourceName.class.getName()))
                .thenReturn(java.util.Collections.singletonMap("name", "value"));

        // Instantiate ConfigSourceNameCondition
        ConfigSourceNameCondition condition = new ConfigSourceNameCondition();

        // Test
        assertTrue(condition.matches(context, metadata));
    }

    @Test
    public void testMatchesWhenPropertyDoesNotMatchAnnotation() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.config-source.name")).thenReturn("value");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        when(metadata.getAnnotationAttributes(ConfigSourceName.class.getName()))
                .thenReturn(java.util.Collections.singletonMap("name", "differentValue"));

        // Instantiate ConfigSourceNameCondition
        ConfigSourceNameCondition condition = new ConfigSourceNameCondition();

        // Test
        assertFalse(condition.matches(context, metadata));
    }

    @Test
    public void testMatchesWhenPropertyIsNull() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.config-source.name")).thenReturn(null);

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);

        // Instantiate ConfigSourceNameCondition
        ConfigSourceNameCondition condition = new ConfigSourceNameCondition();

        // Test
        assertFalse(condition.matches(context, metadata));
    }

    @Test
    public void testMatchesWhenExceptionIsThrown() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.config-source.name")).thenReturn("value");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata to throw exception
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(ConfigSourceName.class.getName()))
                .thenThrow(NullPointerException.class);

        // Instantiate ConfigSourceNameCondition
        ConfigSourceNameCondition condition = new ConfigSourceNameCondition();

        // This should throw NullPointerException
        condition.matches(context, metadata);
    }
}