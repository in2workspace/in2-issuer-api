package es.in2.issuer.backend.backoffice.domain.model.dtos;

public record ApiErrorResponse(String type,
                               String title,
                               int status,
                               String detail,
                               String instance) {
}
