package es.in2.issuer.domain.service;

import es.in2.issuer.domain.exception.UserDoesNotExistException;
import es.in2.issuer.domain.model.CommitCredential;
import es.in2.issuer.domain.model.SubjectDataResponse;
import reactor.core.publisher.Mono;

public interface AuthenticSourcesRemoteService {

    Mono<SubjectDataResponse> getUser(String token) throws UserDoesNotExistException;

    Mono<SubjectDataResponse> getUserFromLocalFile() throws UserDoesNotExistException;

    Mono<Void> commitCredentialSourceData(CommitCredential commitCredential, String token);
}
