package es.in2.issuer.domain.exception.handler;

import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.CredentialErrorResponse;
import es.in2.issuer.domain.model.GlobalErrorMessage;
import es.in2.issuer.domain.util.CredentialResponseErrorCodes;
import es.in2.issuer.infrastructure.configuration.exception.AzureConfigurationSettingException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // fixme: no debería tener el AzureException. Esa exceptión debe ser capturada por el servicio
    //  y este debe lanzar una custom de la propia solución
    @ExceptionHandler(AzureConfigurationSettingException.class)
    public Mono<ResponseEntity<Void>> handleAzureConfigurationSettingError(AzureConfigurationSettingException e) {
        log.error(e.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(CredentialTypeUnsuportedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleCredentialTypeUnsupported(Exception e) {
        String description = "The given credential type is not supported";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
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
    public Mono<ResponseEntity<CredentialErrorResponse>> handleExpiredCacheException(Exception e) {
        String description = "The given credential ID does not match with any credentials";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.VC_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    @ExceptionHandler(ExpiredPreAuthorizedCodeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> expiredPreAuthorizedCode(Exception e) {
        log.error(e.getMessage(), e);

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.EXPIRED_PRE_AUTHORIZED_CODE,
                "The pre-authorized code has expired, has been used, or does not exist.");

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(InvalidOrMissingProofException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleInvalidOrMissingProof(Exception e) {
        log.error(e.getMessage());

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_OR_MISSING_PROOF,
                "Credential Request did not contain a proof, or proof was invalid, " +
                        "i.e. it was not bound to a Credential Issuer provided nonce");

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleInvalidToken(Exception e) {
        log.error(e.getMessage());

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.INVALID_TOKEN,
                "Credential Request contains the wrong Access Token or the Access Token is missing");

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> handleUserDoesNotExistException(Exception e) {
        String description = "User does not exist";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
                CredentialResponseErrorCodes.USER_DOES_NOT_EXIST,
                description);

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(VcTemplateDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<CredentialErrorResponse>> vcTemplateDoesNotExist(Exception e) {
        String description = "The given template name is not supported";

        if (e.getMessage() != null) {
            log.error(e.getMessage());
            description = e.getMessage();
        }

        CredentialErrorResponse errorResponse = new CredentialErrorResponse(
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
    public Mono<ResponseEntity<GlobalErrorMessage>> handleException(Exception ex, WebRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : CredentialResponseErrorCodes.DEFAULT_ERROR;
        log.error(message, ex);

        GlobalErrorMessage customErrorResponse = new GlobalErrorMessage(
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

    @ExceptionHandler(SignedDataParsingException.class)
    public Mono<ResponseEntity<Void>> handleSignedDataParsingException(SignedDataParsingException e) {
        log.error(e.getMessage());
        return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
