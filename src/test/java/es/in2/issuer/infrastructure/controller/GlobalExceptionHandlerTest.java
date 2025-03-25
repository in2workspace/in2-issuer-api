package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.dto.CredentialErrorResponse;
import es.in2.issuer.domain.util.CredentialResponseErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                .assertNext(responseEntity -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()))
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
                    assertEquals("The request contains the wrong Access Token or the Access Token is missing", responseEntity.getBody().description());
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
    void handleUserDoesNotExistException_withMessage() {
        String errorMessage = "Error message for testing";
        UserDoesNotExistException userDoesNotExistException = new UserDoesNotExistException(errorMessage);

        Mono<ResponseEntity<CredentialErrorResponse>> responseEntityMono = globalExceptionHandler.handleUserDoesNotExistException(userDoesNotExistException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.USER_DOES_NOT_EXIST, responseEntity.getBody().error());
                    assertEquals(errorMessage, responseEntity.getBody().description());
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
    void handleVcTemplateDoesNotExist_withMessage() {
        String errorMessage = "Error message for testing";
        VcTemplateDoesNotExistException vcTemplateDoesNotExistException = new VcTemplateDoesNotExistException(errorMessage);

        Mono<ResponseEntity<CredentialErrorResponse>> responseEntityMono = globalExceptionHandler.vcTemplateDoesNotExist(vcTemplateDoesNotExistException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.VC_TEMPLATE_DOES_NOT_EXIST, responseEntity.getBody().error());
                    assertEquals(errorMessage, responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleParseException() {
        ParseException exception = new ParseException("Error message", 213);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleParseException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleBase45Exception() {
        Base45Exception exception = new Base45Exception(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleBase45Exception(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleCreateDate() {
        CreateDateException exception = new CreateDateException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleCreateDateException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleSignedDataParsing() {
        SignedDataParsingException exception = new SignedDataParsingException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleSignedDataParsingException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleAuthenticSourcesUserParsing() {
        AuthenticSourcesUserParsingException exception = new AuthenticSourcesUserParsingException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleSignedDataParsingException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleParseCredentialJsonException() {
        ParseCredentialJsonException exception = new ParseCredentialJsonException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleParseCredentialJsonException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleTemplateReadException() {
        TemplateReadException exception = new TemplateReadException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleTemplateReadException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleProofValidationException() {
        ProofValidationException exception = new ProofValidationException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleProofValidationException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleNoCredentialFoundException() {
        NoCredentialFoundException exception = new NoCredentialFoundException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleNoCredentialFoundException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handlePreAuthorizationCodeGetException() {
        PreAuthorizationCodeGetException exception = new PreAuthorizationCodeGetException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handlePreAuthorizationCodeGetException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleCustomCredentialOfferNotFoundException() {
        CustomCredentialOfferNotFoundException exception = new CustomCredentialOfferNotFoundException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleCustomCredentialOfferNotFoundException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleNonceValidationException() {
        NonceValidationException exception = new NonceValidationException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleNonceValidationException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleOperationNotSupportedException_withMessage() {
        String errorMessage = "Error message for testing";
        OperationNotSupportedException operationNotSupportedException = new OperationNotSupportedException(errorMessage);

        Mono<ResponseEntity<CredentialErrorResponse>> responseEntityMono = globalExceptionHandler.handleOperationNotSupportedException(operationNotSupportedException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.OPERATION_NOT_SUPPORTED, responseEntity.getBody().error());
                    assertEquals(errorMessage, responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleJWTVerificationException() {
        JWTVerificationException exception = new JWTVerificationException("message");

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleJWTVerificationException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void handleResponseUriException_withMessage() {
        String errorMessage = "Error message for testing";
        ResponseUriException responseUriException = new ResponseUriException(errorMessage);

        Mono<ResponseEntity<CredentialErrorResponse>> responseEntityMono = globalExceptionHandler.handleResponseUriException(responseUriException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.RESPONSE_URI_ERROR, responseEntity.getBody().error());
                    assertEquals(errorMessage, responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleFormatUnsupportedException_withMessage() {
        String errorMessage = "Error message for testing";
        FormatUnsupportedException formatUnsupportedException = new FormatUnsupportedException(errorMessage);

        Mono<ResponseEntity<CredentialErrorResponse>> responseEntityMono = globalExceptionHandler.handleFormatUnsupportedException(formatUnsupportedException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.FORMAT_IS_NOT_SUPPORTED, responseEntity.getBody().error());
                    assertEquals(errorMessage, responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleTrustServiceProviderForCertificationsException_withMessage() {
        String errorMessage = "Error message for testing";
        InsufficientPermissionException insufficientPermissionException = new InsufficientPermissionException(errorMessage);

        Mono<ResponseEntity<CredentialErrorResponse>> responseEntityMono = globalExceptionHandler.handleInsufficientPermissionException(insufficientPermissionException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.INSUFFICIENT_PERMISSION, responseEntity.getBody().error());
                    assertEquals(errorMessage, responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleCredentialAlreadyIssuedException() {
        CredentialAlreadyIssuedException exception = new CredentialAlreadyIssuedException(null);

        Mono<ResponseEntity<Void>> result = globalExceptionHandler.handleCredentialAlreadyIssuedException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()))
                .verifyComplete();
    }

    @Test
    void handleMissingIdTokenHeaderException_withMessage() {
        String errorMessage = "Error message for testing";
        MissingIdTokenHeaderException exception = new MissingIdTokenHeaderException(errorMessage);

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.handleMissingIdTokenHeaderException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.MISSING_HEADER, responseEntity.getBody().error());
                    assertEquals(errorMessage, responseEntity.getBody().description());
                })
                .verifyComplete();
    }

    @Test
    void handleMissingIdTokenHeaderException_withoutMessage() {
        MissingIdTokenHeaderException exception = new MissingIdTokenHeaderException(null);

        Mono<ResponseEntity<CredentialErrorResponse>> result = globalExceptionHandler.handleMissingIdTokenHeaderException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(CredentialResponseErrorCodes.MISSING_HEADER, responseEntity.getBody().error());
                    assertEquals("The X-ID-TOKEN header is missing, this header is needed to issuer a Verifiable Certification", responseEntity.getBody().description());
                })
                .verifyComplete();
    }
}