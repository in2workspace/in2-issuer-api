package es.in2.issuer.backoffice.domain.service.impl;

import es.in2.issuer.backoffice.domain.service.CredentialOfferService;
import es.in2.issuer.shared.domain.model.dto.CredentialOfferData;
import es.in2.issuer.shared.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.shared.domain.model.dto.Grant;
import es.in2.issuer.shared.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static es.in2.issuer.shared.domain.util.Constants.*;
import static es.in2.issuer.shared.domain.util.EndpointsConstants.CREDENTIAL_OFFER;
import static es.in2.issuer.shared.domain.util.EndpointsConstants.OPENID_CREDENTIAL_OFFER;
import static es.in2.issuer.shared.domain.util.HttpUtils.ensureUrlHasProtocol;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferServiceImpl implements CredentialOfferService {

    private final AppConfig appConfig;

    @Override
    public Mono<CredentialOfferData> buildCustomCredentialOffer(String credentialType, Grant grant, String employeeEmail, String pin) {
        return Mono.just(

                CredentialOfferData.builder()
                        .credentialOffer(CustomCredentialOffer.builder()
                                .credentialIssuer(appConfig.getIssuerApiExternalDomain())
                                .credentials(List.of(CustomCredentialOffer.Credential.builder()
                                        .format(JWT_VC_JSON)
                                        .types(List.of(credentialType))
                                        .build()
                                ))
                                .credentialConfigurationIds(List.of(LEAR_CREDENTIAL_EMPLOYEE))
                                .grants(Map.of(GRANT_TYPE, grant))
                                .build()
                        )
                        .employeeEmail(employeeEmail)
                        .pin(pin)
                        .build());
    }

    @Override
    public Mono<String> createCredentialOfferUriResponse(String nonce) {
        String url = ensureUrlHasProtocol(appConfig.getIssuerApiExternalDomain() + CREDENTIAL_OFFER + "/" + nonce);
        String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8);
        return Mono.just(OPENID_CREDENTIAL_OFFER + encodedUrl);
    }

}
