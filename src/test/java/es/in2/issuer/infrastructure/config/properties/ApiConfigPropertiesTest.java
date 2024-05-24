//package es.in2.issuer.infrastructure.config.properties;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.context.properties.bind.BindResult;
//import org.springframework.boot.context.properties.bind.Binder;
//import org.springframework.mock.env.MockEnvironment;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class ApiConfigPropertiesTest {
//
//    @Test
//    void bindingProperties() {
//        // Setup a MockEnvironment with your properties
//        MockEnvironment environment = new MockEnvironment();
//        environment.setProperty("iamInternalDomain", "example.com");
//        environment.setProperty("issuerExternalDomain", "issuer.example.com");
//        environment.setProperty("authenticSourcesDomain", "authsources.example.com");
//        environment.setProperty("keyVaultDomain", "keyvault.example.com");
//        environment.setProperty("remoteSignatureDomain", "remotesignature.example.com");
//        environment.setProperty("issuerDid", "issuer-did");
//
//        // Create a Binder instance
//        Binder binder = Binder.get(environment);
//
//        // Bind the properties to an AppConfigurationProperties instance
//        BindResult<ApiProperties> bindResult = binder.bind("", ApiProperties.class);
//
//        // Assert the properties are correctly bound
//        assertThat(bindResult.isBound()).isTrue();
//        ApiProperties apiProperties = bindResult.get();
////        assertThat(apiProperties.iamInternalDomain()).isEqualTo("example.com");
////        assertThat(apiProperties.issuerExternalDomain()).isEqualTo("issuer.example.com");
////        assertThat(apiProperties.authenticSourcesDomain()).isEqualTo("authsources.example.com");
////        assertThat(apiProperties.keyVaultDomain()).isEqualTo("keyvault.example.com");
////        assertThat(apiProperties.remoteSignatureDomain()).isEqualTo("remotesignature.example.com");
////        assertThat(apiProperties.issuerDid()).isEqualTo("issuer-did");
//    }
//}
