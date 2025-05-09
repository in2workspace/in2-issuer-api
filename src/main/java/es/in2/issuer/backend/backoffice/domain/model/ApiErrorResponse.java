package es.in2.issuer.backend.backoffice.domain.model;

public record ApiErrorResponse(String type,
                               String title,
                               int status,
                               String detail,
                               String instance) {
}
