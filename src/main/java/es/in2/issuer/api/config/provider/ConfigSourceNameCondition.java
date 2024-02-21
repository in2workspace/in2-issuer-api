package es.in2.issuer.api.config.provider;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

@Component
public class ConfigSourceNameCondition implements Condition {

    //private final AdapterProperties adapterProperties;

    //public ConfigAdapterCondition(AdapterProperties adapterProperties) {
    //    this.adapterProperties = adapterProperties;
    //}

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String expectedImplementation = "yaml";

        if (expectedImplementation != null) {
            String actualImplementation = (String) metadata.getAnnotationAttributes(ConfigSourceName.class.getName()).get("name");
            return expectedImplementation.equals(actualImplementation);
        }
        return false;
    }
}
