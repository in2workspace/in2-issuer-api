package es.in2.issuer.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.net.HttpHeaders;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InvalidTokenException;
import es.in2.issuer.domain.model.AppNonceValidationResponse;
import es.in2.issuer.domain.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Utils> constructor = Utils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void testToJsonString() throws Exception {
        AppNonceValidationResponse appNonceValidationResponse = new AppNonceValidationResponse("token");

        String jsonString = Utils.toJsonString(appNonceValidationResponse);

        assertNotNull(jsonString);
        assertTrue(jsonString.contains("token"));
    }

    @Test
    void testToJsonStringWithNamingStrategy() throws JsonProcessingException {
        AppNonceValidationResponse appNonceValidationResponse = new AppNonceValidationResponse("token");

        String jsonString = Utils.toJsonString(appNonceValidationResponse, PropertyNamingStrategies.LOWER_CAMEL_CASE);

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
