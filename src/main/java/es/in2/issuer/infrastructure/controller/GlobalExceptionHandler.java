package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.dto.CredentialErrorResponse;
import es.in2.issuer.domain.util.CredentialResponseErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<CredentialErrorResponse>> handleCredentialTypeUnsupported(Exception ex) {
        String description = "The given credential type is not supported";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.UNSUPPORTED_CREDENTIAL_TYPE,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Void>> handleNoSuchElementException(NoSuchElementException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ExpiredCacheException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleExpiredCacheException(Exception ex) {
        String description = "The given credential ID does not match with any credentials";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    @ExceptionHandler(ExpiredPreAuthorizedCodeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> expiredPreAuthorizedCode(Exception ex) {
        log.error(ex.getMessage());

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.EXPIRED_PRE_AUTHORIZED_CODE,
                "The pre-authorized code has expired, has been used, or does not exist.");

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(InvalidOrMissingProofException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleInvalidOrMissingProof(Exception ex) {
        log.error(ex.getMessage());

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_OR_MISSING_PROOF,
                "Credential Request did not contain a proof, or proof was invalid, " +
                        "i.e. it was not bound to a Credential Issuer provided nonce");

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleInvalidToken(Exception ex) {
        String description = "Credential Request contains the wrong Access Token or the Access Token is missing";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_TOKEN,
                description
                );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleUserDoesNotExistException(Exception ex) {
        String description = "User does not exist";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.USER_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(VcTemplateDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> vcTemplateDoesNotExist(Exception ex) {
        String description = "The given template name is not supported";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_TEMPLATE_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(ParseException.class)
    public Mono<ResponseEntity<Void>> handleParseException(ParseException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Base45Exception.class)
    public Mono<ResponseEntity<Void>> handleBase45Exception(Base45Exception ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(CreateDateException.class)
    public Mono<ResponseEntity<Void>> handleCreateDateException(CreateDateException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(SignedDataParsingException.class)
    public Mono<ResponseEntity<Void>> handleSignedDataParsingException(SignedDataParsingException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(AuthenticSourcesUserParsingException.class)
    public Mono<ResponseEntity<Void>> handleSignedDataParsingException(AuthenticSourcesUserParsingException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    @ExceptionHandler(ParseCredentialJsonException.class)
    public Mono<ResponseEntity<Void>> handleParseCredentialJsonException(ParseCredentialJsonException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    @ExceptionHandler(TemplateReadException.class)
    public Mono<ResponseEntity<Void>> handleTemplateReadException(TemplateReadException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    @ExceptionHandler(ProofValidationException.class)
    public Mono<ResponseEntity<Void>> handleProofValidationException(ProofValidationException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    @ExceptionHandler(NoCredentialFoundException.class)
    public Mono<ResponseEntity<Void>> handleNoCredentialFoundException(NoCredentialFoundException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @ExceptionHandler(PreAuthorizationCodeGetException.class)
    public Mono<ResponseEntity<Void>> handlePreAuthorizationCodeGetException(PreAuthorizationCodeGetException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    @ExceptionHandler(CustomCredentialOfferNotFoundException.class)
    public Mono<ResponseEntity<Void>> handleCustomCredentialOfferNotFoundException(CustomCredentialOfferNotFoundException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @ExceptionHandler(NonceValidationException.class)
    public Mono<ResponseEntity<Void>> handleNonceValidationException(NonceValidationException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    @ExceptionHandler(CredentialAlreadyIssuedException.class)
    public Mono<ResponseEntity<Void>> handleCredentialAlreadyIssuedException(CredentialAlreadyIssuedException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.CONFLICT));
    }

    @ExceptionHandler(OperationNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleOperationNotSupportedException(Exception ex) {
        String description = "The given operation is not supported";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.OPERATION_NOT_SUPPORTED,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(JWTVerificationException.class)
    public Mono<ResponseEntity<Void>> handleJWTVerificationException(JWTVerificationException ex) {
        log.error(ex.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(ResponseUriException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleResponseUriException(Exception ex) {
        String description = "Request to response uri failed";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.RESPONSE_URI_ERROR,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    @ExceptionHandler(FormatUnsupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleFormatUnsupportedException(Exception ex) {
        String description = "Format is not supported";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.FORMAT_IS_NOT_SUPPORTED,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(InsufficientPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleInsufficientPermissionException(Exception ex) {
        String description = "The client who made the issuance request do not have the required permissions";

        if (ex.getMessage() != null) {
            log.error(ex.getMessage());
            description = ex.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INSUFFICIENT_PERMISSION,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }

    @ExceptionHandler(UnauthorizedRoleException.class)
    public ResponseEntity<String> handleUnauthorizedRoleException(UnauthorizedRoleException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
