package es.in2.issuer.infrastructure.iam.util;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IamSourceNameConditionTest {
    @Test
    void testMatchesWhenPropertyMatchesAnnotation() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.iam-source.name")).thenReturn("value");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        when(metadata.getAnnotationAttributes(IamSourceName.class.getName()))
                .thenReturn(java.util.Collections.singletonMap("name", "value"));

        // Instantiate IamSourceNameCondition
        IamSourceNameCondition condition = new IamSourceNameCondition();

        // Test
        assertTrue(condition.matches(context, metadata));
    }

    @Test
    void testMatchesWhenPropertyDoesNotMatchAnnotation() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.iam-source.name")).thenReturn("value");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        when(metadata.getAnnotationAttributes(IamSourceName.class.getName()))
                .thenReturn(java.util.Collections.singletonMap("name", "differentValue"));

        // Instantiate IamSourceNameCondition
        IamSourceNameCondition condition = new IamSourceNameCondition();

        // Test
        assertFalse(condition.matches(context, metadata));
    }

    @Test
    void testMatchesWhenPropertyIsNull() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.iam-source.name")).thenReturn(null);

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);

        // Instantiate IamSourceNameCondition
        IamSourceNameCondition condition = new IamSourceNameCondition();

        // Test
        assertFalse(condition.matches(context, metadata));
    }

    @Test
    void testMatchesWhenExceptionIsThrown() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.iam-source.name")).thenReturn("value");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        when(metadata.getAnnotationAttributes(IamSourceName.class.getName()))
                .thenThrow(new RuntimeException("Exception occurred"));

        // Instantiate IamSourceNameCondition
        IamSourceNameCondition condition = new IamSourceNameCondition();

        // Test
        assertFalse(condition.matches(context, metadata));
    }
}
