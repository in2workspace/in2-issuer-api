package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> sendEmailNotification(@RequestParam("procedure_id") String procedureId) {
        String processId = UUID.randomUUID().toString();
        return notificationService.sendNotification(processId, procedureId);
    }
}
