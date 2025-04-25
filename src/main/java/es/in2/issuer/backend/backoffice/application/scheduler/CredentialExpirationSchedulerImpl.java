package es.in2.issuer.backend.backoffice.application.scheduler;

import es.in2.issuer.backend.shared.domain.model.entities.CredentialProcedure;
import es.in2.issuer.backend.shared.domain.model.enums.CredentialStatus;
import es.in2.issuer.backend.shared.infrastructure.repository.CredentialProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.sql.Timestamp;
import java.time.Instant;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class CredentialExpirationSchedulerImpl implements CredentialExpirationScheduler {

    private final CredentialProcedureRepository credentialProcedureRepository;

    @Override
    @Scheduled(cron = "0 0 1 * * ?") //Every day at 1:00 AM
    public Mono<Void> checkAndExpireCredentials() {
        log.info("Scheduled Task - Executing checkAndExpireCredentials at: {}", Instant.now());

        return credentialProcedureRepository.findAll()
                .flatMap(credential -> isExpired(credential)
                        .filter(Boolean::booleanValue)
                        .flatMap(expired -> expireCredential(credential)))
                .then();
    }

    private Mono<Boolean> isExpired(CredentialProcedure credentialProcedure) {
        return Mono.justOrEmpty(credentialProcedure.getValidUntil())
                .map(validUntil -> validUntil.toInstant().isBefore(Instant.now()))
                .defaultIfEmpty(false);
    }

    Mono<CredentialProcedure> expireCredential(CredentialProcedure credentialProcedure) {
        if(credentialProcedure.getCredentialStatus() != CredentialStatus.EXPIRED) {
            credentialProcedure.setCredentialStatus(CredentialStatus.EXPIRED);
            credentialProcedure.setUpdatedAt(Timestamp.from(Instant.now()));
            log.info("Expiring credential with ID: {} - New state: {}",
                    credentialProcedure.getCredentialId(),
                    credentialProcedure.getCredentialStatus());
            return credentialProcedureRepository.save(credentialProcedure);
        }
        return Mono.empty();
    }
}