package es.in2.issuer.backend.backoffice.application.scheduler;
import reactor.core.publisher.Mono;

public interface CredentialExpirationScheduler {
    Mono<Void> checkAndExpireCredentials();
}