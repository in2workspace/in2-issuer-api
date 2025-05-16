package es.in2.issuer.backend.shared.domain.service.impl;

import es.in2.issuer.backend.shared.domain.model.dto.ResponseUriRequest;
import es.in2.issuer.backend.shared.domain.service.CredentialDeliveryService;
import es.in2.issuer.backend.shared.domain.service.EmailService;
import es.in2.issuer.backend.shared.infrastructure.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CredentialDeliveryServiceImpl implements CredentialDeliveryService {

    private final WebClient webClient;
    private final EmailService emailService;
    private final AppConfig appConfig;

    public CredentialDeliveryServiceImpl(WebClient.Builder webClientBuilder,
                                         EmailService emailService,
                                         AppConfig appConfig) {
        this.webClient = webClientBuilder.build();
        this.emailService = emailService;
        this.appConfig = appConfig;
    }

    @Override
    public Mono<Void> sendVcToResponseUri(String responseUri, String encodedVc, String productId, String companyEmail, String bearerToken) {
        log.info("Sending VC to responseUri")
        ResponseUriRequest responseUriRequest = ResponseUriRequest.builder()
                .encodedVc(encodedVc)
                .build();
        //log.debug
        log.info("Sending to response_uri: {} the VC: {} with token: {}", responseUri, encodedVc, bearerToken);

        return webClient.patch()
                .uri(responseUri)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .bodyValue(responseUriRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        if (HttpStatus.ACCEPTED.equals(response.statusCode())) {
                            log.info("Received 202 from response_uri. Extracting HTML and sending specific mail for missing documents");
                            return response.bodyToMono(String.class)
                                    .flatMap(htmlResponseBody ->
                                            emailService.sendResponseUriAcceptedWithHtml(companyEmail, productId, htmlResponseBody))
                                    .then();
                        }
                        return Mono.empty();
                    } else {
                        log.error("Non-2xx status code received: {}. Sending failure email...", response.statusCode());
                        return emailService.sendResponseUriFailed(companyEmail, productId, appConfig.getKnowledgeBaseUploadCertificationGuideUrl())
                                .then();
                    }
                })
                .onErrorResume(WebClientRequestException.class, ex -> {
                    log.error("Network error while sending VC to response_uri", ex);
                    return emailService.sendResponseUriFailed(companyEmail, productId, appConfig.getKnowledgeBaseUploadCertificationGuideUrl())
                            .then();
                });
    }
}
