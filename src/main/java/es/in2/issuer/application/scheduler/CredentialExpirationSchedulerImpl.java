package es.in2.issuer.application.scheduler;

import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static es.in2.issuer.domain.util.Constants.*;

import java.time.Instant;


@Service
@EnableScheduling
public class CredentialExpirationSchedulerImpl implements CredentialExpirationScheduler {

    private final CredentialProcedureRepository credentialProcedureRepository;
    private final ObjectMapper objectMapper;

    public CredentialExpirationSchedulerImpl(CredentialProcedureRepository credentialProcedureRepository, ObjectMapper objectMapper) {
        this.credentialProcedureRepository = credentialProcedureRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?") //Cada día a la 1:00 AM
    public void checkAndExpireCredentials() {
        System.out.println("Scheduled Task - Ejecutando checkAndExpireCredentials a: " + Instant.now());

        credentialProcedureRepository.findAll()
                .filter(this::isExpired)
                .flatMap(this::expireCredential)
                .publishOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private boolean isExpired(CredentialProcedure credentialProcedure) {
        try {
            JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
            JsonNode validUntilNode = credential.get(VC).get("validUntil");

            if (validUntilNode != null && validUntilNode.isTextual()) {
                Instant validUntil = Instant.parse(validUntilNode.asText());
                return validUntil.isBefore(Instant.now());
            } else {
                return false;
            }

        } catch (JsonProcessingException e) {
            return false;
        }
    }

    Mono<CredentialProcedure> expireCredential(CredentialProcedure credentialProcedure) {
        credentialProcedure.setCredentialStatus(CredentialStatus.EXPIRED);
        return credentialProcedureRepository.save(credentialProcedure);
    }
}