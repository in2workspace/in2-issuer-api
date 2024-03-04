package es.in2.issuer.api.service.impl;

import es.in2.issuer.api.config.AppConfiguration;
import es.in2.issuer.api.model.dto.CredentialIssuerMetadata;
import es.in2.issuer.api.model.dto.CredentialsSupportedParameter;
import es.in2.issuer.api.service.CredentialIssuerMetadataService;
import id.walt.credentials.w3c.templates.VcTemplateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static es.in2.issuer.api.util.Constants.LEAR_CREDENTIAL;
import static es.in2.issuer.api.util.HttpUtils.ensureUrlHasProtocol;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialIssuerMetadataServiceImpl implements CredentialIssuerMetadataService {

    private final AppConfiguration appConfiguration;
    private String issuerApiBaseUrl;
    private String keycloakUrl;
    private String did;

    @PostConstruct
    private void initializeIssuerApiBaseUrl() {
        issuerApiBaseUrl = appConfiguration.getIssuerDomain();
        keycloakUrl = appConfiguration.getKeycloakExternalDomain();
        did = appConfiguration.getKeycloakDid();
    }
    @Override
    public Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer() {
        String issuerApiBaseUrlWithProtocol = ensureUrlHasProtocol(issuerApiBaseUrl);
        String tokenUri = keycloakUrl + "/realms/EAAProvider/verifiable-credential/" + did + "/token";

        return Mono.just(new CredentialIssuerMetadata(
                issuerApiBaseUrlWithProtocol,
                issuerApiBaseUrlWithProtocol + "/api/vc/type/",
                tokenUri,
                generateCredentialsSupportedList()
        ));
    }

    private List<CredentialsSupportedParameter> generateCredentialsSupportedList() {
        return Arrays.asList(
                new CredentialsSupportedParameter(
                        "jwt_vc_json",
                        "VerifiableId_JWT",
                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "VerifiableId"),
                        List.of("did"),
                        new ArrayList<>(),
                        VcTemplateService.Companion.getService().getTemplate("VerifiableId",true,VcTemplateService.SAVED_VC_TEMPLATES_KEY)
                ),
                new CredentialsSupportedParameter(
                        "jwt_vc_json",
                        LEAR_CREDENTIAL,
                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "LEARCredential"),
                        List.of("did"),
                        new ArrayList<>(),
                        VcTemplateService.Companion.getService().getTemplate("LEARCredential",true,VcTemplateService.SAVED_VC_TEMPLATES_KEY)
                )
        );
    }
}
