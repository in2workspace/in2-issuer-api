package es.in2.issuer.backend.backoffice.domain.service;

import es.in2.issuer.backend.backoffice.domain.exception.UserDoesNotExistException;
import es.in2.issuer.backend.backoffice.domain.model.dto.CommitCredential;
import es.in2.issuer.backend.backoffice.domain.model.dto.SubjectDataResponse;
import reactor.core.publisher.Mono;

public interface AuthenticSourcesRemoteService {

    Mono<SubjectDataResponse> getUser(String token) throws UserDoesNotExistException;

    Mono<String> getUserFromLocalFile() throws UserDoesNotExistException;

    Mono<Void> commitCredentialSourceData(CommitCredential commitCredential, String token);
}
