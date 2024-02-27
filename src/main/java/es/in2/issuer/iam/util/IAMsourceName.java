package es.in2.issuer.iam.util;

import es.in2.issuer.configuration.model.ConfigProviderName;
import es.in2.issuer.configuration.util.ConfigSourceNameCondition;
import es.in2.issuer.iam.model.IAMproviderName;
import org.springframework.context.annotation.Conditional;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(IAMsourceNameCondition.class)
public @interface IAMsourceName {
    IAMproviderName name();
}
