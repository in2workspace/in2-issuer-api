package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.CredentialDetails;
import es.in2.issuer.domain.model.dto.CredentialItem;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CredentialProcedureService {
    Mono<String> createCredentialProcedure(CredentialProcedureCreationRequest credentialProcedureCreationRequest);
    Mono<String> getCredentialTypeByProcedureId(String procedureId);
    Mono<Void> updateDecodedCredentialByProcedureId(String procedureId, String credential);
    Mono<String> getDecodedCredentialByProcedureId(String procedureId);
    Mono<String> getMandateeEmailFromDecodedCredentialByProcedureId (String procedureId);
    Mono<String> getMandatorEmailFromDecodedCredentialByProcedureId(String procedureId);
    Flux<String> getAllIssuedCredentialByOrganizationIdentifier(String organizationIdentifier);
    Flux<CredentialItem> getAllCredentialByOrganizationIdentifier(String organizationIdentifier);
    Mono<CredentialDetails> getCredentialByProcedureIdAndOrganizationId(String procedureId, String organizationIdentifier);
    Mono<String> updatedEncodedCredentialByCredentialId(String encodedCredential, String credentialId);
}
