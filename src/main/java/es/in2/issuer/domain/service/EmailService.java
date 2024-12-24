package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface EmailService {
    Mono<Void> sendPin(String to, String subject, String pin);
    Mono<Void> sendTransactionCodeForCredentialOffer(String to, String subject, String link, String knowledgebaseUrl, String user, String organization);
    Mono<Void> sendPendingCredentialNotification(String to, String subject);
    Mono<Void> sendCredentialSignedNotification(String to, String subject, String name);
}
