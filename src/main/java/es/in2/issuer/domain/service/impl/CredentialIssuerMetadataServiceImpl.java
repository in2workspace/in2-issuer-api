package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.domain.model.CredentialsSupported;
import es.in2.issuer.domain.model.VcTemplate;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL;
import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialIssuerMetadataServiceImpl implements CredentialIssuerMetadataService {

    private final IamAdapterFactory iamAdapterFactory;
    private final AppConfiguration appConfiguration;
    // fixme: this is a temporary solution to load credential templates from resources
    @Value("classpath:credentials/templates/LEARCredentialTemplate.json")
    private Resource learCredentialTemplate;
    @Value("classpath:credentials/templates/VerifiableIdTemplate.json")
    private Resource verifiableIdTemplate;

    // fixme: ¿Por qué hay un postconstruct aquí con el issuerAPIBaseUrl?
    private String issuerApiBaseUrl;
    @PostConstruct
    private void initializeIssuerApiBaseUrl() {
        issuerApiBaseUrl = appConfiguration.getIssuerDomain();
    }

    @Override
    public Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer() {
        String credentialIssuerDomain = ensureUrlHasProtocol(issuerApiBaseUrl);
        return Mono.just(CredentialIssuerMetadata.builder().credentialIssuer(credentialIssuerDomain)
                // fixme: Este path debe capturarse de la configuración
                .credentialEndpoint(credentialIssuerDomain + "/api/vc/credential").credentialToken(iamAdapterFactory.getAdapter().getTokenUri()).credentialsSupported(generateCredentialsSupportedList()).build());
    }

    private List<CredentialsSupported> generateCredentialsSupportedList() {
        // Injecting templates from local files:
        VcTemplate learCredentialVcTemplate;
        VcTemplate VerifiableIdVcTemplate;
        try {
            learCredentialVcTemplate = VcTemplate.builder().mutable(true).name(LEAR_CREDENTIAL).template(new String(learCredentialTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).build();
            VerifiableIdVcTemplate = VcTemplate.builder().mutable(true).name("VerifiableId").template(new String(verifiableIdTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CredentialsSupported verifiableIdJWT = CredentialsSupported.builder().format("jwt_vc_json").id("VerifiableId_JWT").types(Arrays.asList("VerifiableCredential", "VerifiableAttestation", "VerifiableId")).cryptographicBindingMethodsSupported(List.of("did")).cryptographicSuitesSupported(List.of()).credentialSubject(learCredentialVcTemplate).build();
        CredentialsSupported learCredential = CredentialsSupported.builder().format("jwt_vc_json").id(LEAR_CREDENTIAL).types(Arrays.asList("VerifiableCredential", "VerifiableAttestation", "LEARCredential")).cryptographicBindingMethodsSupported(List.of("did")).cryptographicSuitesSupported(List.of()).credentialSubject(VerifiableIdVcTemplate).build();
        return List.of(verifiableIdJWT, learCredential);
    }

}
