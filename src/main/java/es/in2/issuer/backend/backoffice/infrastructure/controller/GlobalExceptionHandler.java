package es.in2.issuer.backend.backoffice.infrastructure.controller;


import es.in2.issuer.backend.backoffice.domain.exception.*;
import es.in2.issuer.backend.backoffice.domain.util.CredentialResponseErrorCodes;
import es.in2.issuer.backend.shared.domain.exception.*;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialErrorResponse;
import es.in2.issuer.backend.shared.domain.model.dto.GlobalErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredentialTypeUnsupportedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<CredentialErrorResponse> handleCredentialTypeUnsupported(Exception ex) {
        String description = "The given credential type is not supported";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.UNSUPPORTED_CREDENTIAL_TYPE,
                description);

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Void> handleNoSuchElementException(NoSuchElementException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(ExpiredCacheException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<CredentialErrorResponse> handleExpiredCacheException(Exception ex) {
        String description = "The given credential ID does not match with any credentials";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_DOES_NOT_EXIST,
                description);

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(ExpiredPreAuthorizedCodeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<CredentialErrorResponse> expiredPreAuthorizedCode(Exception ex) {
        log.error(ex.getMessage());

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.EXPIRED_PRE_AUTHORIZED_CODE,
                "The pre-authorized code has expired, has been used, or does not exist.");

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(InvalidOrMissingProofException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<CredentialErrorResponse> handleInvalidOrMissingProof(Exception ex) {
        log.error(ex.getMessage());

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_OR_MISSING_PROOF,
                "Credential Request did not contain a proof, or proof was invalid, " +
                        "i.e. it was not bound to a Credential Issuer provided nonce");

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<CredentialErrorResponse> handleInvalidToken(Exception ex) {
        String description = "Credential Request contains the wrong Access Token or the Access Token is missing";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_TOKEN,
                description
                );

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<CredentialErrorResponse> handleUserDoesNotExistException(Exception ex) {
        String description = "User does not exist";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.USER_DOES_NOT_EXIST,
                description);

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(VcTemplateDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<CredentialErrorResponse> vcTemplateDoesNotExist(Exception ex) {
        String description = "The given template name is not supported";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_TEMPLATE_DOES_NOT_EXIST,
                description);

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(ParseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleParseException(ParseException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(Base45Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleBase45Exception(Base45Exception ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(CreateDateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleCreateDateException(CreateDateException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(SignedDataParsingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleSignedDataParsingException(SignedDataParsingException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(AuthenticSourcesUserParsingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleSignedDataParsingException(AuthenticSourcesUserParsingException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }
    
    @ExceptionHandler(ParseCredentialJsonException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleParseCredentialJsonException(ParseCredentialJsonException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(TemplateReadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleTemplateReadException(TemplateReadException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(ProofValidationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handleProofValidationException(ProofValidationException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(NoCredentialFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Void> handleNoCredentialFoundException(NoCredentialFoundException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(PreAuthorizationCodeGetException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Void> handlePreAuthorizationCodeGetException(PreAuthorizationCodeGetException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }
    
    @ExceptionHandler(CredentialOfferNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Void> handleCustomCredentialOfferNotFoundException(CredentialOfferNotFoundException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(CredentialAlreadyIssuedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<Void> handleCredentialAlreadyIssuedException(CredentialAlreadyIssuedException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(OperationNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<CredentialErrorResponse> handleOperationNotSupportedException(Exception ex) {
        String description = "The given operation is not supported";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.OPERATION_NOT_SUPPORTED,
                description);

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(JWTVerificationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<Void> handleJWTVerificationException(JWTVerificationException ex) {
        log.error(ex.getMessage());
        return Mono.empty();
    }

    @ExceptionHandler(ResponseUriException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<CredentialErrorResponse> handleResponseUriException(Exception ex) {
        String description = "Request to response uri failed";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.RESPONSE_URI_ERROR,
                description);

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(InsufficientPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<CredentialErrorResponse> handleInsufficientPermissionException(Exception ex) {
        String description = "The client who made the issuance request do not have the required permissions";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INSUFFICIENT_PERMISSION,
                description);

        return Mono.just(errorResponse);
    }

    @ExceptionHandler(EmailCommunicationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<GlobalErrorMessage> handleEmailCommunicationException(EmailCommunicationException ex) {

        return Mono.just(
                GlobalErrorMessage.builder()
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .message(ex.getMessage())
                        .error("EmailCommunicationException")
                        .build());
    }
}
