package es.in2.issuer.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.application.service.LEARCredentialWithdrawService;
import es.in2.issuer.domain.model.LEARCredentialEmployee;
import es.in2.issuer.domain.model.LEARCredentialRequest;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE_TYPES;

@Service
@RequiredArgsConstructor
@Slf4j
public class LEARCredentialWithdrawServiceImpl implements LEARCredentialWithdrawService {
    private final ObjectMapper objectMapper;
    private final AppConfiguration appConfiguration;
    @Override
    public Mono<Void> completeWithdrawLearCredentialProcess(String processId, LEARCredentialRequest learCredentialRequest) {
        try {
            LEARCredentialEmployee learCredentialEmployee = objectMapper.convertValue(learCredentialRequest.credential(), LEARCredentialEmployee.class);
            Instant currentTime = Instant.now();
            LEARCredentialEmployee compleatedWithdrawLEARCredentialEmployee = LEARCredentialEmployee.builder()
                    .expirationDate(currentTime.plus(30, ChronoUnit.DAYS).toString())
                    .issuanceDate(currentTime.toString())
                    .validFrom(currentTime.toString())
                    .id(UUID.randomUUID().toString())
                    .type(LEAR_CREDENTIAL_EMPLOYEE_TYPES)
                    .issuer(appConfiguration.getIssuerDid())
                    .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                            .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                    .mandator(learCredentialEmployee.credentialSubject().mandate().mandator())
                                    .mandatee(learCredentialEmployee.credentialSubject().mandate().mandatee())
                                    .power(learCredentialEmployee.credentialSubject().mandate().power())
                                    .lifeSpan(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder()
                                            .startDateTime(currentTime.toString())
                                            .endDateTime(currentTime.plus(30, ChronoUnit.DAYS).toString())
                                            .build())
                                    .build())
                            .build())
                    .build();
            log.debug(compleatedWithdrawLEARCredentialEmployee.toString());
            return Mono.empty();
            
        } catch (Exception e){
            return Mono.error(new RuntimeException());
        }

    }
}
