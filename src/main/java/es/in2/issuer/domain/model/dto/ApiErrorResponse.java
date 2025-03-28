package es.in2.issuer.domain.model.dto;

public record ApiErrorResponse(String type,
                               String title,
                               int status,
                               String detail,
                               String instance) {
}
