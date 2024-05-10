package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.Grant;
import es.in2.issuer.domain.service.CredentialOfferService;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.EndpointsConstants.*;
import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;


@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferServiceImpl implements CredentialOfferService {

    private final AppConfiguration appConfiguration;

    @Override
    public Mono<CustomCredentialOffer> buildCustomCredentialOffer(String credentialType, Grant grant) {
        return Mono.just(CustomCredentialOffer.builder()
                .credentialIssuer(appConfiguration.getIssuerExternalDomain())
                .credentials(List.of(
                        CustomCredentialOffer.Credential.builder().format(JWT_VC).types(List.of(credentialType)).build()
                ))
                .credentialConfigurationIds(List.of(LEAR_CREDENTIAL_JWT))
                .grants(Map.of(GRANT_TYPE, grant))
                .build());
    }

    @Override
    public Mono<String> createCredentialOfferUri(String nonce) {
        String url = ensureUrlHasProtocol(appConfiguration.getIssuerExternalDomain() + CREDENTIAL_OFFER + nonce);
        String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8);
        return Mono.just(OPENID_CREDENTIAL_OFFER + encodedUrl);
    }

}
