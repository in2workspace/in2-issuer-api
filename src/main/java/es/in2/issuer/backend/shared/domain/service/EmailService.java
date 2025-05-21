package es.in2.issuer.backend.shared.domain.service;

import reactor.core.publisher.Mono;

public interface EmailService {
    Mono<Void> sendTxCodeNotification(String to, String subject, String pin);
    Mono<Void> sendCredentialActivationEmail(String to, String subject, String link, String knowledgebaseWalletUrl, String user, String organization);
    Mono<Void> sendPendingCredentialNotification(String to, String subject);
    Mono<Void> sendCredentialSignedNotification(String to, String subject, String name, String additionalInfo);
    Mono<Void> sendResponseUriFailed(String to, String productId, String guideUrl);
    Mono<Void> sendResponseUriAcceptedWithHtml(String to, String productId, String htmlContent);
    Mono<Void> sendPendingSignatureCredentialNotification(String to, String subject, String id, String domain);
}
