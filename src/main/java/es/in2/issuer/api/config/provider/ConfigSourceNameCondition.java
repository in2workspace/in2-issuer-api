package es.in2.issuer.api.config.provider;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConfigSourceNameCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String expectedImplementation = context.getEnvironment().getProperty("app.config-source.name");

        if (expectedImplementation != null) {
            try {
                String actualImplementation = metadata.getAnnotationAttributes(ConfigSourceName.class.getName()).get("name").toString();
                return expectedImplementation.equals(actualImplementation);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
