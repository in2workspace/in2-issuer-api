package es.in2.issuer.backend.backoffice.infrastructure.controller;


import es.in2.issuer.backend.backoffice.domain.exception.*;
import es.in2.issuer.backend.backoffice.domain.util.CredentialResponseErrorCodes;
import es.in2.issuer.backend.shared.domain.exception.*;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialErrorResponse;
import es.in2.issuer.backend.shared.domain.model.dto.GlobalErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        WebRequest mockWebRequest = mock(WebRequest.class);
        when(mockWebRequest.getDescription(false)).thenReturn("WebRequestDescription");
    }

    @Test
    void handleCredentialTypeUnsupported() {
        CredentialTypeUnsupportedException exception = new CredentialTypeUnsupportedException("The given credential type is not supported");

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.UNSUPPORTED_CREDENTIAL_TYPE,
                exception.getMessage()
        );

        Mono<CredentialErrorResponse> result = globalExceptionHandler.handleCredentialTypeUnsupported(exception);

        StepVerifier
                .create(result)
                .assertNext(credentialErrorResponse ->
                        assertThat(credentialErrorResponse).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleNoSuchElement() {
        NoSuchElementException exception = new NoSuchElementException();

        Mono<Void> result = globalExceptionHandler.handleNoSuchElementException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleExpiredCache() {
        ExpiredCacheException exception = new ExpiredCacheException("The given credential ID does not match with any credentials");

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_DOES_NOT_EXIST,
                exception.getMessage()
        );

        Mono<CredentialErrorResponse> result = globalExceptionHandler.handleExpiredCacheException(exception);

        StepVerifier
                .create(result)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleExpiredPreAuthorizedCode() {
        ExpiredPreAuthorizedCodeException exception = new ExpiredPreAuthorizedCodeException("Error message");

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.EXPIRED_PRE_AUTHORIZED_CODE,
                "The pre-authorized code has expired, has been used, or does not exist."
        );

        Mono<CredentialErrorResponse> result = globalExceptionHandler.expiredPreAuthorizedCode(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleInvalidOrMissingProof() {
        InvalidOrMissingProofException exception = new InvalidOrMissingProofException("Error message");

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_OR_MISSING_PROOF,
                "Credential Request did not contain a proof, or proof was invalid, i.e. it was not bound to a Credential Issuer provided nonce"
        );

        Mono<CredentialErrorResponse> result = globalExceptionHandler.handleInvalidOrMissingProof(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleInvalidToken() {
        InvalidTokenException exception = new InvalidTokenException();

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_TOKEN,
                exception.getMessage()
        );

        Mono<CredentialErrorResponse> result = globalExceptionHandler.handleInvalidToken(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleUserDoesNotExist() {
        UserDoesNotExistException exception = new UserDoesNotExistException(null);

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.USER_DOES_NOT_EXIST,
                "User does not exist"
        );

        Mono<CredentialErrorResponse> result = globalExceptionHandler.handleUserDoesNotExistException(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleUserDoesNotExistException_withMessage() {
        String errorMessage = "Error message for testing";
        UserDoesNotExistException userDoesNotExistException = new UserDoesNotExistException(errorMessage);

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.USER_DOES_NOT_EXIST,
                userDoesNotExistException.getMessage()
        );

        Mono<CredentialErrorResponse> responseEntityMono = globalExceptionHandler.handleUserDoesNotExistException(userDoesNotExistException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleVcTemplateDoesNotExist() {
        VcTemplateDoesNotExistException exception = new VcTemplateDoesNotExistException("error message");

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_TEMPLATE_DOES_NOT_EXIST,
                exception.getMessage()
        );

        Mono<CredentialErrorResponse> result = globalExceptionHandler.vcTemplateDoesNotExist(exception);

        StepVerifier.create(result)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleVcTemplateDoesNotExist_withMessage() {
        String errorMessage = "Error message for testing";
        VcTemplateDoesNotExistException vcTemplateDoesNotExistException = new VcTemplateDoesNotExistException(errorMessage);

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_TEMPLATE_DOES_NOT_EXIST,
                vcTemplateDoesNotExistException.getMessage()
        );

        Mono<CredentialErrorResponse> responseEntityMono = globalExceptionHandler.vcTemplateDoesNotExist(vcTemplateDoesNotExistException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleParseException() {
        ParseException exception = new ParseException("Error message", 213);

        Mono<Void> result = globalExceptionHandler.handleParseException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleBase45Exception() {
        Base45Exception exception = new Base45Exception(null);

        Mono<Void> result = globalExceptionHandler.handleBase45Exception(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleCreateDate() {
        CreateDateException exception = new CreateDateException(null);

        Mono<Void> result = globalExceptionHandler.handleCreateDateException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleSignedDataParsing() {
        SignedDataParsingException exception = new SignedDataParsingException(null);

        Mono<Void> result = globalExceptionHandler.handleSignedDataParsingException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleAuthenticSourcesUserParsing() {
        AuthenticSourcesUserParsingException exception = new AuthenticSourcesUserParsingException(null);

        Mono<Void> result = globalExceptionHandler.handleSignedDataParsingException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleParseCredentialJsonException() {
        ParseCredentialJsonException exception = new ParseCredentialJsonException(null);

        Mono<Void> result = globalExceptionHandler.handleParseCredentialJsonException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleTemplateReadException() {
        TemplateReadException exception = new TemplateReadException(null);

        Mono<Void> result = globalExceptionHandler.handleTemplateReadException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleProofValidationException() {
        ProofValidationException exception = new ProofValidationException(null);

        Mono<Void> result = globalExceptionHandler.handleProofValidationException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleNoCredentialFoundException() {
        NoCredentialFoundException exception = new NoCredentialFoundException(null);

        Mono<Void> result = globalExceptionHandler.handleNoCredentialFoundException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handlePreAuthorizationCodeGetException() {
        PreAuthorizationCodeGetException exception = new PreAuthorizationCodeGetException(null);

        Mono<Void> result = globalExceptionHandler.handlePreAuthorizationCodeGetException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleCustomCredentialOfferNotFoundException() {
        CredentialOfferNotFoundException exception = new CredentialOfferNotFoundException(null);

        Mono<Void> result = globalExceptionHandler.handleCustomCredentialOfferNotFoundException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleOperationNotSupportedException_withMessage() {
        String errorMessage = "Error message for testing";
        OperationNotSupportedException operationNotSupportedException = new OperationNotSupportedException(errorMessage);

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.OPERATION_NOT_SUPPORTED,
                operationNotSupportedException.getMessage()
        );

        Mono<CredentialErrorResponse> responseEntityMono = globalExceptionHandler.handleOperationNotSupportedException(operationNotSupportedException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleJWTVerificationException() {
        JWTVerificationException exception = new JWTVerificationException("message");

        Mono<Void> result = globalExceptionHandler.handleJWTVerificationException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleResponseUriException_withMessage() {
        String errorMessage = "Error message for testing";
        ResponseUriException responseUriException = new ResponseUriException(errorMessage);

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.RESPONSE_URI_ERROR,
                responseUriException.getMessage()
        );

        Mono<CredentialErrorResponse> responseEntityMono = globalExceptionHandler.handleResponseUriException(responseUriException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleFormatUnsupportedException_withMessage() {
        String errorMessage = "Error message for testing";
        FormatUnsupportedException formatUnsupportedException = new FormatUnsupportedException(errorMessage);

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.FORMAT_IS_NOT_SUPPORTED,
                formatUnsupportedException.getMessage()
        );

        Mono<CredentialErrorResponse> responseEntityMono = globalExceptionHandler.handleFormatUnsupportedException(formatUnsupportedException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleTrustServiceProviderForCertificationsException_withMessage() {
        String errorMessage = "Error message for testing";
        InsufficientPermissionException insufficientPermissionException = new InsufficientPermissionException(errorMessage);

        var expected = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INSUFFICIENT_PERMISSION,
                insufficientPermissionException.getMessage()
        );

        Mono<CredentialErrorResponse> responseEntityMono = globalExceptionHandler.handleInsufficientPermissionException(insufficientPermissionException);

        StepVerifier.create(responseEntityMono)
                .assertNext(responseEntity ->
                        assertThat(responseEntity).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleCredentialAlreadyIssuedException() {
        CredentialAlreadyIssuedException exception = new CredentialAlreadyIssuedException(null);

        Mono<Void> result = globalExceptionHandler.handleCredentialAlreadyIssuedException(exception);

        StepVerifier.create(result)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void handleEmailCommunicationException_withMessage() {
        String errorMessage = "Notification service unavailable";
        EmailCommunicationException exception = new EmailCommunicationException(errorMessage);

        var expected = GlobalErrorMessage.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(exception.getMessage())
                .error("EmailCommunicationException")
                .build();

        Mono<GlobalErrorMessage> result = globalExceptionHandler.handleEmailCommunicationException(exception);

        StepVerifier.create(result)
                .assertNext(globalErrorMessage ->
                        assertThat(globalErrorMessage).isEqualTo(expected))
                .verifyComplete();
    }

    @Test
    void handleEmailCommunicationException_withoutMessage() {
        EmailCommunicationException exception = new EmailCommunicationException(null);

        Mono<GlobalErrorMessage> result = globalExceptionHandler.handleEmailCommunicationException(exception);

        StepVerifier.create(result)
                .assertNext(globalErrorMessage -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), globalErrorMessage.status());
                    assertNull(globalErrorMessage.message());
                })
                .verifyComplete();
    }
}