package es.in2.issuer.backoffice.application.scheduler;
import reactor.core.publisher.Mono;

public interface CredentialExpirationScheduler {
    Mono<Void> checkAndExpireCredentials();
}