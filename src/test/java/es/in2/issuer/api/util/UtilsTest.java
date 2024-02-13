package es.in2.issuer.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.net.HttpHeaders;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.api.model.dto.AppNonceValidationResponseDTO;
import es.in2.issuer.api.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void testConstructor() {
        assertThrows(IllegalStateException.class, Utils::new);
    }

    @Test
    void testToJsonString() throws Exception {
        AppNonceValidationResponseDTO appNonceValidationResponseDTO = new AppNonceValidationResponseDTO("token");

        String jsonString = Utils.toJsonString(appNonceValidationResponseDTO);

        assertNotNull(jsonString);
        assertTrue(jsonString.contains("token"));
    }

    @Test
    void testToJsonStringWithNamingStrategy() throws JsonProcessingException {
        AppNonceValidationResponseDTO appNonceValidationResponseDTO = new AppNonceValidationResponseDTO("token");

        String jsonString = Utils.toJsonString(appNonceValidationResponseDTO, PropertyNamingStrategies.LOWER_CAMEL_CASE);

        assertNotNull(jsonString);
        assertTrue(jsonString.contains("token"));
    }

    @Test
    void testGetToken() throws InvalidTokenException, ParseException {
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        SignedJWT signedJWT = Utils.getToken(exchange);

        assertNotNull(signedJWT);
        assertEquals("1234567890", signedJWT.getJWTClaimsSet().getSubject());
    }

    @Test
    void testGetTokenWithInvalidAuthorizationHeader() {
        String token = "InvalidToken";
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThrows(InvalidTokenException.class, () -> Utils.getToken(exchange));
    }

    @Test
    void testGetTokenWithNullAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThrows(InvalidTokenException.class, () -> Utils.getToken(exchange));
    }

    @Test
    void testGetTokenWithMalformedToken() {
        String token = "Bearer InvalidToken";
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThrows(InvalidTokenException.class, () -> Utils.getToken(exchange));
    }

    @Test
    void testCreateDate() throws ParseException {
        String dateString = "2024-02-07T23:59:59Z";

        Date date = Utils.createDate(dateString);

        assertNotNull(date);
    }

    @Test
    void testIsNullOrBlank() {
        String blankString = "";
        String nonBlankString = "test";

        assertTrue(Utils.isNullOrBlank(blankString));
        assertFalse(Utils.isNullOrBlank(nonBlankString));
    }

}
