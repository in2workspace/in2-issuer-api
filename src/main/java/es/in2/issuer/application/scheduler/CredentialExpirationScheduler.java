package es.in2.issuer.application.scheduler;

public interface CredentialExpirationScheduler {
    void checkAndExpireCredentials();
}