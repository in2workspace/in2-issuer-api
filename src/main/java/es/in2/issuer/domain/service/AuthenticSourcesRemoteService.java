package es.in2.issuer.domain.service;

import es.in2.issuer.domain.exception.UserDoesNotExistException;
import es.in2.issuer.domain.model.IDEPCommitCredential;
import es.in2.issuer.domain.model.SubjectDataResponse;
import reactor.core.publisher.Mono;

public interface AuthenticSourcesRemoteService {

    Mono<SubjectDataResponse> getUser(String token) throws UserDoesNotExistException;

    Mono<SubjectDataResponse> getUserFromLocalFile();

    Mono<Void> commitCredentialSourceData(IDEPCommitCredential idepCommitCredential, String token);
}
