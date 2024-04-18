package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.Grant;
import es.in2.issuer.domain.service.CredentialOfferService;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;


@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferServiceImpl implements CredentialOfferService {

    private final AppConfiguration appConfiguration;

    @Override
    public Mono<CustomCredentialOffer> buildCustomCredentialOffer(String credentialType, String preAuthCode) {
        return Mono.just(CustomCredentialOffer.builder()
                .credentialIssuer(appConfiguration.getIssuerExternalDomain())
                .credentials(List.of(
                        new CustomCredentialOffer.Credential(JWT_VC, List.of(credentialType))
                ))
                .credentialConfigurationIds(List.of(LEAR_CREDENTIAL_JWT))
                .grants(Map.of(GRANT_TYPE, new Grant(preAuthCode, false, new Grant.TxCode(4,"numeric", "Please enter the PIN code"))))
                .build());
    }

    @Override
    public Mono<String> createCredentialOfferUri(String nonce) {
        return Mono.just(
                ensureUrlHasProtocol(appConfiguration.getIssuerExternalDomain() + "/credential-offer?credential_offer_uri=") +
                        ensureUrlHasProtocol(appConfiguration.getIssuerExternalDomain() + "/credential-offer/" + nonce)
        );
    }

}
