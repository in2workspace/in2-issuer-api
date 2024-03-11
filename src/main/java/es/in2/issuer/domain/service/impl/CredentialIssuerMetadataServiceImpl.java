package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.domain.model.CredentialsSupported;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import id.walt.credentials.w3c.templates.VcTemplateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        CredentialsSupported verifiableIdJWT = CredentialsSupported.builder().format("jwt_vc_json").id("VerifiableId_JWT").types(Arrays.asList("VerifiableCredential", "VerifiableAttestation", "VerifiableId")).cryptographicBindingMethodsSupported(List.of("did")).cryptographicSuitesSupported(List.of()).credentialSubject(VcTemplateService.Companion.getService().getTemplate("VerifiableId", true, VcTemplateService.SAVED_VC_TEMPLATES_KEY)).build();
        CredentialsSupported learCredential = CredentialsSupported.builder().format("jwt_vc_json").id(LEAR_CREDENTIAL).types(Arrays.asList("VerifiableCredential", "VerifiableAttestation", "LEARCredential")).cryptographicBindingMethodsSupported(List.of("did")).cryptographicSuitesSupported(List.of()).credentialSubject(VcTemplateService.Companion.getService().getTemplate("LEARCredential", true, VcTemplateService.SAVED_VC_TEMPLATES_KEY)).build();
        return List.of(verifiableIdJWT, learCredential);
    }

}
