package es.in2.issuer.backend.backoffice.domain.service;

import es.in2.issuer.backend.backoffice.domain.model.CompleteSignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.SignatureConfigWithProviderName;
import es.in2.issuer.backend.backoffice.domain.model.SignatureConfigurationResponse;
import es.in2.issuer.backend.shared.domain.model.entities.SignatureConfiguration;
import es.in2.issuer.backend.shared.domain.model.enums.SignatureMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface SignatureConfigurationService {
    Mono<SignatureConfiguration> saveSignatureConfig(CompleteSignatureConfiguration config, String organizationIdentifier);
    Mono<Map<String,Object>> getSignatureCredentials(String secretRelativePath);
    Flux<SignatureConfigWithProviderName> findAllByOrganizationIdentifierAndMode(String organizationIdentifier, SignatureMode signatureMode);
    Mono<SignatureConfigurationResponse> getCompleteConfigurationById(String id);
    Mono<Void> updateSignatureConfiguration(String id, CompleteSignatureConfiguration config , String rationale ,String userEmail);
    Mono<Void> deleteSignatureConfiguration(String id, String rationale, String userEmail);
}
