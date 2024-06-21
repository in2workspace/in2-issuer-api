package es.in2.issuer.domain.model.dto;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LEARCredentialEmployeeTest {

    private static LEARCredentialEmployee.@NotNull CredentialSubject getCredentialSubject() {
        LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan lifeSpan = new LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan("2024-01-01T00:00:00", "2024-12-31T23:59:59");
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = new LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee("mandateeId", "email", "firstName", "lastName", "123456789");
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = getMandate(lifeSpan, mandatee);
        return new LEARCredentialEmployee.CredentialSubject(mandate);
    }

    private static LEARCredentialEmployee.CredentialSubject.@NotNull Mandate getMandate(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan lifeSpan, LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee) {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator = new LEARCredentialEmployee.CredentialSubject.Mandate.Mandator("commonName", "country", "emailAddress", "organization", "organizationIdentifier", "serialNumber");
        List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> powerList = Collections.singletonList(new LEARCredentialEmployee.CredentialSubject.Mandate.Power("powerId", "tmfAction", "tmfDomain", "tmfFunction", "tmfType"));
        return new LEARCredentialEmployee.CredentialSubject.Mandate("mandateId", lifeSpan, mandatee, mandator, powerList);
    }

    @Test
    void testConstructorAndGetters() {
        //Arrange
        List<String> expectedContext = List.of("context1", "context2");
        String expectedId = "id";
        List<String> expectedType = List.of("type1", "type2");
        LEARCredentialEmployee.CredentialSubject credentialSubject = getCredentialSubject();
        String expectedExpirationDate = "expirationDate";
        String expectedIssuanceDate = "issuanceDate";
        String expectedIssuer = "issuer";
        String expectedValidFrom = "validFrom";

        LEARCredentialEmployee credentialEmployee = LEARCredentialEmployee.builder()
                .context(expectedContext)
                .id(expectedId)
                .type(expectedType)
                .credentialSubject(credentialSubject)
                .expirationDate(expectedExpirationDate)
                .issuanceDate(expectedIssuanceDate)
                .issuer(expectedIssuer)
                .validFrom(expectedValidFrom)
                .build();

        // Act & Assert
        assertEquals(expectedContext, credentialEmployee.context());
        assertEquals(expectedId, credentialEmployee.id());
        assertEquals(expectedType, credentialEmployee.type());
        assertEquals(credentialSubject, credentialEmployee.credentialSubject());
        assertEquals(expectedExpirationDate, credentialEmployee.expirationDate());
        assertEquals(expectedIssuanceDate, credentialEmployee.issuanceDate());
        assertEquals(expectedIssuer, credentialEmployee.issuer());
        assertEquals(expectedValidFrom, credentialEmployee.validFrom());
    }

}