package es.in2.issuer.infrastructure.iam.util;

import es.in2.issuer.infrastructure.iam.model.IamProviderName;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(IamSourceNameCondition.class)
public @interface IamSourceName {
    IamProviderName name();
}
