package es.in2.issuer.api.config.provider;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConfigSourceNameCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String expectedImplementation = context.getEnvironment().getProperty("app.config-source.name");

        if (expectedImplementation != null) {
            String actualImplementation = (String) metadata.getAnnotationAttributes(ConfigSourceName.class.getName()).get("name");
            return expectedImplementation.equals(actualImplementation);
        }
        return false;
    }
}
