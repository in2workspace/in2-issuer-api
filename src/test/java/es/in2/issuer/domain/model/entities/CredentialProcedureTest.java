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

    @Test
    void testSettersAndGetters() {
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        UUID procedureId = UUID.randomUUID();
        UUID credentialId = UUID.randomUUID();
        String credentialFormat = "format";
        String credentialDecoded = "decoded";
        String credentialEncoded = "encoded";
        CredentialStatus credentialStatus = CredentialStatus.VALID;
        String organizationIdentifier = "orgId";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        credentialProcedure.setProcedureId(procedureId);
        credentialProcedure.setCredentialId(credentialId);
        credentialProcedure.setCredentialFormat(credentialFormat);
        credentialProcedure.setCredentialDecoded(credentialDecoded);
        credentialProcedure.setCredentialEncoded(credentialEncoded);
        credentialProcedure.setCredentialStatus(credentialStatus);
        credentialProcedure.setOrganizationIdentifier(organizationIdentifier);
        credentialProcedure.setUpdatedAt(timestamp);

        assertEquals(procedureId, credentialProcedure.getProcedureId());
        assertEquals(credentialId, credentialProcedure.getCredentialId());
        assertEquals(credentialFormat, credentialProcedure.getCredentialFormat());
        assertEquals(credentialDecoded, credentialProcedure.getCredentialDecoded());
        assertEquals(credentialEncoded, credentialProcedure.getCredentialEncoded());
        assertEquals(credentialStatus, credentialProcedure.getCredentialStatus());
        assertEquals(organizationIdentifier, credentialProcedure.getOrganizationIdentifier());
        assertEquals(timestamp, credentialProcedure.getUpdatedAt());
    }

    @Test
    void testToString() {
        CredentialProcedure credentialProcedure = CredentialProcedure.builder().build();

        String expected = "CredentialProcedure(procedureId=" + credentialProcedure.getProcedureId() +
                ", credentialId=" + credentialProcedure.getCredentialId() +
                ", credentialFormat=" + credentialProcedure.getCredentialFormat() +
                ", credentialDecoded=" + credentialProcedure.getCredentialDecoded() +
                ", credentialEncoded=" + credentialProcedure.getCredentialEncoded() +
                ", credentialStatus=" + credentialProcedure.getCredentialStatus() +
                ", organizationIdentifier=" + credentialProcedure.getOrganizationIdentifier() +
                ", updatedAt=" + credentialProcedure.getUpdatedAt() +
                ", subject=" + credentialProcedure.getSubject() +
                ", credentialType=" + credentialProcedure.getCredentialType() +
                ")";
        assertEquals(expected, credentialProcedure.toString());
    }
}