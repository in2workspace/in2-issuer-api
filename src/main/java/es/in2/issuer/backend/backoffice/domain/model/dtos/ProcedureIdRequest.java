package es.in2.issuer.backend.backoffice.domain.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcedureIdRequest(@JsonProperty(value = "procedure-id") String procedureId) {
}
