package es.in2.issuer.domain.model.entities;

import es.in2.issuer.domain.model.enums.CredentialStatus;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialProcedureTest {

    @Test
    void testCredentialProcedure() {
        UUID procedureId = UUID.randomUUID();
        UUID credentialId = UUID.randomUUID();
        String credentialFormat = "testFormat";
        String credentialDecoded = "testDecoded";
        String credentialEncoded = "testEncoded";
        CredentialStatus credentialStatus = CredentialStatus.VALID;
        String organizationIdentifier = "testOrganizationIdentifier";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        CredentialProcedure credentialProcedure = CredentialProcedure.builder()
                .procedureId(procedureId)
                .credentialId(credentialId)
                .credentialFormat(credentialFormat)
                .credentialDecoded(credentialDecoded)
                .credentialEncoded(credentialEncoded)
                .credentialStatus(credentialStatus)
                .organizationIdentifier(organizationIdentifier)
                .updatedAt(timestamp)
                .build();

        assertEquals(procedureId, credentialProcedure.getProcedureId());
        assertEquals(credentialId, credentialProcedure.getCredentialId());
        assertEquals(credentialFormat, credentialProcedure.getCredentialFormat());
        assertEquals(credentialDecoded, credentialProcedure.getCredentialDecoded());
        assertEquals(credentialEncoded, credentialProcedure.getCredentialEncoded());
        assertEquals(credentialStatus, credentialProcedure.getCredentialStatus());
        assertEquals(organizationIdentifier, credentialProcedure.getOrganizationIdentifier());
        assertEquals(timestamp, credentialProcedure.getUpdatedAt());
    }
}