package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.credential.lear.machine.LEARCredentialMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LEARCredentialMachineFactory {
    private final ObjectMapper objectMapper;

    public LEARCredentialMachine mapStringToLEARCredentialMachine(String learCredential)
            throws InvalidCredentialFormatException {
        try {
            log.info(objectMapper.readValue(learCredential, LEARCredentialMachine.class).toString());
            return objectMapper.readValue(learCredential, LEARCredentialMachine.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing LEARCredentialMachine", e);
            throw new InvalidCredentialFormatException("Error parsing LEARCredentialMachine");
        }
    }
}
