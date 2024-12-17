package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface EmailService {
    Mono<Void> sendPin(String to, String subject, String pin);
    Mono<Void> sendTransactionCodeForCredentialOffer(String to, String subject, String link, String firstName, String walletUrl);
    Mono<Void> sendPendingCredentialNotification(String to, String subject);
    Mono<Void> sendCredentialSignedNotification(String to, String subject, String name);
    Mono<Void> sendResponseUriFailed(String to, String productId, String guideUrl);
    Mono<Void> sendResponseUriAcceptedWithHtml(String to, String productId, String htmlContent);
}
