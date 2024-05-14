package es.in2.issuer.infrastructure.iam.util;

import es.in2.issuer.infrastructure.iam.model.IamProviderName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IamSourceNameTest {

    @Test
    void testConditionMatches() {
        // Mock Environment
        Environment environment = mock(Environment.class);
        when(environment.getProperty("app.iam-source.name")).thenReturn("keycloak");

        // Mock ConditionContext
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        // Mock AnnotationMetadata
        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        when(metadata.getAnnotationAttributes(IamSourceName.class.getName()))
                .thenReturn(java.util.Collections.singletonMap("name", "keycloak"));


        // Create instance of IamSourceNameCondition
        IamSourceNameCondition condition = new IamSourceNameCondition();

        // Mock the behavior to return KEYCLOAK as the expected IamProviderName
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", IamProviderName.KEYCLOAK);
        when(metadata.getAnnotationAttributes(IamSourceName.class.getName()))
                .thenReturn(attributes);

        // Evaluate the condition
        boolean matches = condition.matches(context, metadata);

        // Assertion
        Assertions.assertTrue(matches, "Condition should match for KEYCLOAK IAM provider");
    }

}
