package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.CompleteSignatureConfiguration;
import es.in2.issuer.domain.model.entities.SignatureConfiguration;
import es.in2.issuer.domain.model.enums.SignatureMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface SignatureConfigurationService {
    Mono<SignatureConfiguration> saveSignatureConfig(CompleteSignatureConfiguration config, String organizationIdentifier);
    Mono<Map<String,Object>> getSignatureCredentials(String secretRelativePath);
    Flux<SignatureConfiguration> findAllByOrganizationIdentifierAndMode(String organizationIdentifier, SignatureMode signatureMode);
    Mono<CompleteSignatureConfiguration> getCompleteConfigurationById(String id);
    Mono<Void> updateSignatureConfiguration(String id, CompleteSignatureConfiguration config , String rationale ,String userEmail);
    Mono<Void> deleteSignatureConfiguration(String id, String rationale, String userEmail);
}
