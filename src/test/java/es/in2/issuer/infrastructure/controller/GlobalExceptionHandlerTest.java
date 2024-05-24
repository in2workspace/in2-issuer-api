package es.in2.issuer.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.dto.CredentialErrorResponse;
import es.in2.issuer.domain.model.dto.GlobalErrorMessage;
import es.in2.issuer.domain.util.CredentialResponseErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest mockWebRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        mockWebRequest = mock(WebRequest.class);
        when(mockWebRequest.getDescription(false)).thenReturn("WebRequestDescription");
    }

    @Test
    void handleCredentialTypeUnsupported() {
        CredentialTypeUnsupportedException exception = new CredentialTypeUnsupportedException("The given credential type is not supported");

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.handleCredentialTypeUnsupported(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.UNSUPPORTED_CREDENTIAL_TYPE, responseEntity.getBody().error());
                    assertEquals("The given credential type is not supported", responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleNoSuchElement() {
        NoSuchElementException exception = new NoSuchElementException();

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleNoSuchElementException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleExpiredCache() {
        ExpiredCacheException exception = new ExpiredCacheException("The given credential ID does not match with any credentials");

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.handleExpiredCacheException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.VC_DOES_NOT_EXIST, responseEntity.getBody().error());
                    assertEquals("The given credential ID does not match with any credentials", responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleExpiredPreAuthorizedCode() {
        ExpiredPreAuthorizedCodeException exception = new ExpiredPreAuthorizedCodeException("Error message");

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.expiredPreAuthorizedCode(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.EXPIRED_PRE_AUTHORIZED_CODE, responseEntity.getBody().error());
                    assertEquals("The pre-authorized code has expired, has been used, or does not exist.", responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleInvalidOrMissingProof() {
        InvalidOrMissingProofException exception = new InvalidOrMissingProofException("Error message");

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.handleInvalidOrMissingProof(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.INVALID_OR_MISSING_PROOF, responseEntity.getBody().error());
                    assertEquals("Credential Request did not contain a proof, or proof was invalid, " +
                            "i.e. it was not bound to a Credential Issuer provided nonce", responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleInvalidToken() {
        InvalidTokenException exception = new InvalidTokenException();

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.handleInvalidToken(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.INVALID_TOKEN, responseEntity.getBody().error());
                    assertEquals("Credential Request contains the wrong Access Token or the Access Token is missing", responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleUserDoesNotExist() {
        UserDoesNotExistException exception = new UserDoesNotExistException(null);

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.handleUserDoesNotExistException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.USER_DOES_NOT_EXIST, responseEntity.getBody().error());
                    assertEquals("User does not exist", responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleVcTemplateDoesNotExist() {
        VcTemplateDoesNotExistException exception = new VcTemplateDoesNotExistException(null);

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.vcTemplateDoesNotExist(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.VC_TEMPLATE_DOES_NOT_EXIST, responseEntity.getBody().error());
                    assertEquals("The given template name is not supported", responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleParseException() {
        ParseException exception = new ParseException("Error message", 213);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleParseException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleException_ReturnsInternalServerError() {
        Exception exception = new Exception("General error occurred");
        Mono<ResponseEntity<GlobalErrorMessage>> result = globalExceptionHandler.handleException(exception, mockWebRequest);
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().status());
                    assertEquals("General error occurred", responseEntity.getBody().message());
                    assertTrue(LocalDateTime.now().minusMinutes(1).isBefore(responseEntity.getBody().timestamp()) &&
                                    LocalDateTime.now().plusMinutes(1).isAfter(responseEntity.getBody().timestamp()),
                            "Timestamp is not within the expected range");
                })
                .verifyComplete();
    }

    @Test
    void handleBase45Exception() {
        Base45Exception exception = new Base45Exception(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleBase45Exception(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleCreateDate() {
        CreateDateException exception = new CreateDateException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleCreateDateException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleSignedDataParsing() {
        SignedDataParsingException exception = new SignedDataParsingException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleSignedDataParsingException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleAuthenticSourcesUserParsing() {
        AuthenticSourcesUserParsingException exception = new AuthenticSourcesUserParsingException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleSignedDataParsingException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleParseCredentialJsonException() {
        ParseCredentialJsonException exception = new ParseCredentialJsonException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleParseCredentialJsonException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleTemplateReadException() {
        TemplateReadException exception = new TemplateReadException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleTemplateReadException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleProofValidationException() {
        ProofValidationException exception = new ProofValidationException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleProofValidationException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleNoCredentialFoundException() {
        NoCredentialFoundException exception = new NoCredentialFoundException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleNoCredentialFoundException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handlePreAuthorizationCodeGetException() {
        PreAuthorizationCodeGetException exception = new PreAuthorizationCodeGetException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handlePreAuthorizationCodeGetException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleCustomCredentialOfferNotFoundException() {
        CustomCredentialOfferNotFoundException exception = new CustomCredentialOfferNotFoundException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleCustomCredentialOfferNotFoundException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleNonceValidationException() {
        NonceValidationException exception = new NonceValidationException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleNonceValidationException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

}