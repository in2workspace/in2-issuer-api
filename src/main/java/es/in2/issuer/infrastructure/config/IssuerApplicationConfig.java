package es.in2.issuer.infrastructure.config;

import es.in2.issuer.domain.model.CredentialOfferForPreAuthorizedCodeFlow;
import es.in2.issuer.domain.model.VerifiableCredentialJWT;
import es.in2.issuer.domain.util.Utils;
import es.in2.issuer.infrastructure.repository.CacheStore;
import id.walt.credentials.w3c.VerifiableCredential;
import id.walt.credentials.w3c.templates.VcTemplateService;
import id.walt.servicematrix.ServiceMatrix;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class IssuerApplicationConfig {

    // fixme: this is a temporary solution to load credential templates from resources
    @Value("classpath:credentials/LEARCredentialExample.json")
    private Resource learCredentialResource;

    // fixme: this is a temporary solution to load credential templates from resources
    @Value("classpath:credentials/CustomerCredentialExample.json")
    private Resource customerCredentialResource;

    @PostConstruct
    public void addCustomerCredentialTemplateToInMemoryVcLibrary() {
        // todo: delete the use of Walt.id libraries
        // Load walt.id SSI-Kit services from workingDirectory service-matrix.properties
        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);
        try {
            String learCredentialJsonData = new String(learCredentialResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            VerifiableCredential learCredential = VerifiableCredential.Companion.fromJson(learCredentialJsonData);
            VcTemplateService.Companion.getService().register("LEARCredential", learCredential);
            log.debug("LEARCredential added to VC Lib");

            String customerCredentialJsonData = new String(customerCredentialResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            VerifiableCredential customerCredential = VerifiableCredential.Companion.fromJson(customerCredentialJsonData);
            VcTemplateService.Companion.getService().register("CustomerCredential", customerCredential);
            log.debug("CustomerCredential added to VC Lib");
        } catch (IOException e) {
            log.error("Error loading credential templates: {}", e.getMessage());
        }
    }

    @Bean
    public CacheStore<String> cacheStoreForString() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<VerifiableCredentialJWT> cacheStoreForVerifiableCredentialJwt() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<CredentialOfferForPreAuthorizedCodeFlow> cacheStoreForCredentialOffer() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

}
