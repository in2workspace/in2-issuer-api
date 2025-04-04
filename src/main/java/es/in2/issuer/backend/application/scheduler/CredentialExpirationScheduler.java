package es.in2.issuer.backend.application.scheduler;
import reactor.core.publisher.Mono;

public interface CredentialExpirationScheduler {
    Mono<Void> checkAndExpireCredentials();
}