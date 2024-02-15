package es.in2.issuer.api.exception.handler;

import es.in2.issuer.api.model.CredentialResponseErrorCodes;
import es.in2.issuer.api.model.dto.CredentialResponseError;
import es.in2.issuer.api.model.dto.GenericResponseError;
import es.in2.issuer.api.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;


@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(AzureConfigurationSettingException.class)
    public Mono<ResponseEntity<Void>> handleAzureConfigurationSettingError(AzureConfigurationSettingException e) {
        log.error(e.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(CredentialTypeUnsuportedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialResponseError>> handleCredentialTypeUnsupported(Exception e) {
        String description = "The given credential type is not supported";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialResponseError errorResponse = new CredentialResponseError(
                CredentialResponseErrorCodes.UNSUPPORTED_CREDENTIAL_TYPE,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Void>> handleNoSuchElementException(NoSuchElementException e) {
        log.error(e.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ExpiredCacheException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<CredentialResponseError>> handleExpiredCacheException(Exception e) {
        String description = "The given credential ID does not match with any credentials";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialResponseError errorResponse = new CredentialResponseError(
                CredentialResponseErrorCodes.VC_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    @ExceptionHandler(ExpiredPreAuthorizedCodeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialResponseError>> expiredPreAuthorizedCode(Exception e) {
        log.error(e.getMessage(), e);

        CredentialResponseError errorResponse = new CredentialResponseError(
                CredentialResponseErrorCodes.EXPIRED_PRE_AUTHORIZED_CODE,
                "The pre-authorized code has expired, has been used, or does not exist.");

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(InvalidOrMissingProofException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialResponseError>> handleInvalidOrMissingProof(Exception e) {
        log.error(e.getMessage());

        CredentialResponseError errorResponse = new CredentialResponseError(
                CredentialResponseErrorCodes.INVALID_OR_MISSING_PROOF,
                "Credential Request did not contain a proof, or proof was invalid, " +
                        "i.e. it was not bound to a Credential Issuer provided nonce");

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialResponseError>> handleInvalidToken(Exception e) {
        log.error(e.getMessage());

        CredentialResponseError errorResponse = new CredentialResponseError(
                CredentialResponseErrorCodes.INVALID_TOKEN,
                "Credential Request contains the wrong Access Token or the Access Token is missing");

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialResponseError>> handleUserDoesNotExistException(Exception e) {
        String description = "User does not exist";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialResponseError errorResponse = new CredentialResponseError(
                CredentialResponseErrorCodes.USER_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(VcTemplateDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialResponseError>> vcTemplateDoesNotExist(Exception e) {
        String description = "The given template name is not supported";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialResponseError errorResponse = new CredentialResponseError(
                CredentialResponseErrorCodes.VC_TEMPLATE_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(ParseException.class)
    public Mono<ResponseEntity<Void>> handleParseException(ParseException e) {
        log.error(e.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<GenericResponseError>> handleException(Exception ex, WebRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : CredentialResponseErrorCodes.DEFAULT_ERROR;
        log.error(message, ex);

        GenericResponseError customErrorResponse = new GenericResponseError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                message,
                request.getDescription(false)
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(customErrorResponse));
    }

    @ExceptionHandler(Base45Exception.class)
    public Mono<ResponseEntity<Void>> handleBase45Exception(Base45Exception e) {
        log.error(e.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(CreateDateException.class)
    public Mono<ResponseEntity<Void>> handleCreateDateException(CreateDateException e) {
        log.error(e.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
