package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.CredentialDetails;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.CredentialProcedures;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CredentialProcedureService {
    Mono<String> createCredentialProcedure(CredentialProcedureCreationRequest credentialProcedureCreationRequest);

    Mono<String> getCredentialTypeByProcedureId(String procedureId);

    Mono<String> getCredentialStatusByProcedureId(String procedureId);

    Mono<Void> updateDecodedCredentialByProcedureId(String procedureId, String credential, String format);

    Mono<String> getDecodedCredentialByProcedureId(String procedureId);

    Mono<String> getEncodedCredentialByProcedureId(String procedureId);

    Mono<String> getCredentialSubjectEmailFromDecodedCredentialByProcedureId(String procedureId);

    Mono<String> getCredentialSubjectNameFromDecodedCredentialByProcedureId(String procedureId);

    Mono<String> getSignerEmailFromDecodedCredentialByProcedureId(String procedureId);

    Flux<String> getAllIssuedCredentialByOrganizationIdentifier(String organizationIdentifier);

    Mono<CredentialProcedures> getAllProceduresBasicInfoByOrganizationId(String organizationIdentifier);

    Mono<CredentialDetails> getProcedureDetailByProcedureIdAndOrganizationId(String organizationIdentifier, String procedureId);

    Mono<Void> updateCredentialProcedureCredentialStatusToValidByProcedureId(String procedureId);

    Mono<String> updatedEncodedCredentialByCredentialId(String encodedCredential, String credentialId);
}
