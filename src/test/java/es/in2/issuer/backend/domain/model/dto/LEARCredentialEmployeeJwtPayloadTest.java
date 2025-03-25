/*
package es.in2.issuer.domain.model.dto;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//TODO
class LEARCredentialEmployeeJwtPayloadTest {

    List<String> expectedContext = List.of("context1", "context2");
    String expectedId = "id";
    List<String> expectedType = List.of("type1", "type2");
//    LEARCredentialEmployee.CredentialSubject credentialSubject = getCredentialSubject();
    String expectedExpirationDate = "expirationDate";
    String expectedIssuanceDate = "issuanceDate";
    String expectedIssuer = "issuer";
    String expectedValidFrom = "validFrom";
    LEARCredentialEmployee learCredentialEmployee = LEARCredentialEmployee.builder()
            .context(expectedContext)
            .id(expectedId)
            .type(expectedType)
//            .credentialSubject(credentialSubject)
            .expirationDate(expectedExpirationDate)
            .issuanceDate(expectedIssuanceDate)
            .issuer(expectedIssuer)
            .validFrom(expectedValidFrom)
            .build();

    */
/*private static LEARCredentialEmployee.@NotNull CredentialSubject getCredentialSubject() {
        LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan lifeSpan = new LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan("2024-01-01T00:00:00", "2024-12-31T23:59:59");
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = new LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee("mandateeId", "email", "firstName", "lastName", "123456789");
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = getMandate(lifeSpan, mandatee);
        return new LEARCredentialEmployee.CredentialSubject(mandate);
    }*//*


    */
/*private static LEARCredentialEmployee.CredentialSubject.@NotNull Mandate getMandate(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan lifeSpan, LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee) {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator = new LEARCredentialEmployee.CredentialSubject.Mandate.Mandator("commonName", "country", "emailAddress", "organization", "organizationIdentifier", "serialNumber");
        List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> powerList = Collections.singletonList(new LEARCredentialEmployee.CredentialSubject.Mandate.Power("powerId", "tmfAction", "tmfDomain", "tmfFunction", "tmfType"));
        return new LEARCredentialEmployee.CredentialSubject.Mandate("mandateId", lifeSpan, mandatee, mandator, powerList);
    }*//*


    @Test
    void testConstructorAndGetters() {
        // Arrange
        String subject = "subject";
        Long expectedNotValidBefore = 1622548800L;
        String issuer = "issuer";
        Long expectedExpirationTime = 1625130800L;
        Long expectedIssuedAt = 1622548800L;
        LEARCredentialEmployee learCredentialEmployee1 = learCredentialEmployee;
        String expectedJwtId = "jwtId";

        // Act
        LEARCredentialEmployeeJwtPayload payload = new LEARCredentialEmployeeJwtPayload(
                subject,
                expectedNotValidBefore,
                issuer,
                expectedExpirationTime,
                expectedIssuedAt,
                learCredentialEmployee1,
                expectedJwtId
        );

        // Assert
        assertEquals(subject, payload.subject());
        assertEquals(expectedNotValidBefore, payload.notValidBefore());
        assertEquals(issuer, payload.issuer());
        assertEquals(expectedExpirationTime, payload.expirationTime());
        assertEquals(expectedIssuedAt, payload.issuedAt());
        assertEquals(learCredentialEmployee1, payload.learCredentialEmployee());
        assertEquals(expectedJwtId, payload.JwtId());
    }

    @Test
    void testSetters() {
        // Arrange
        String newSubject = "newSubject";
        Long newNotValidBefore = 1622548801L;
        String newIssuer = "newIssuer";
        Long newExpirationTime = 1625130801L;
        Long newIssuedAt = 1622548801L;
        LEARCredentialEmployee learCredentialEmployee1 = learCredentialEmployee;
        String newJwtId = "newJwtId";

        // Act
        LEARCredentialEmployeeJwtPayload payload = LEARCredentialEmployeeJwtPayload.builder()
                .subject(newSubject)
                .notValidBefore(newNotValidBefore)
                .issuer(newIssuer)
                .expirationTime(newExpirationTime)
                .issuedAt(newIssuedAt)
                .learCredentialEmployee(learCredentialEmployee1)
                .JwtId(newJwtId)
                .build();

        // Assert
        assertEquals(newSubject, payload.subject());
        assertEquals(newNotValidBefore, payload.notValidBefore());
        assertEquals(newIssuer, payload.issuer());
        assertEquals(newExpirationTime, payload.expirationTime());
        assertEquals(newIssuedAt, payload.issuedAt());
        assertEquals(learCredentialEmployee1, payload.learCredentialEmployee());
        assertEquals(newJwtId, payload.JwtId());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        String subject = "subject";
        Long expectedNotValidBefore = 1622548800L;
        String issuer = "issuer";
        LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload = getLearCredentialEmployeeJwtPayload(subject, expectedNotValidBefore, issuer);

        // Assert
        assertEquals(learCredentialEmployeeJwtPayload.hashCode(), learCredentialEmployeeJwtPayload.hashCode());
    }

    private @NotNull LEARCredentialEmployeeJwtPayload getLearCredentialEmployeeJwtPayload(String expectedSubject, Long expectedNotValidBefore, String expectedIssuer) {
        Long expectedExpirationTime = 1625130800L;
        Long expectedIssuedAt = 1622548800L;
        LEARCredentialEmployee learCredentialEmployee1 = learCredentialEmployee;
        String jwtId = "jwtId";

        return new LEARCredentialEmployeeJwtPayload(
                expectedSubject,
                expectedNotValidBefore,
                expectedIssuer,
                expectedExpirationTime,
                expectedIssuedAt,
                learCredentialEmployee1,
                jwtId
        );
    }

}*/
