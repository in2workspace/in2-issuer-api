package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> sendNotification(String processId,String procedureId);
}
