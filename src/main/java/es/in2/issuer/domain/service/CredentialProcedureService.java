package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import reactor.core.publisher.Mono;

public interface CredentialProcedureService {
    Mono<String> createCredentialProcedure(CredentialProcedureCreationRequest credentialProcedureCreationRequest);
    Mono<String> getCredentialTypeByProcedureId(String procedureId);
    Mono<Void> updateDecodedCredentialByProcedureId(String procedureId, String credential);
    Mono<String> getDecodedCredentialByProcedureId(String procedureId);
    Mono<String> getMandateeEmailFromDecodedCredentialByProcedureId (String procedureId);
}
