package es.in2.issuer.backend.backoffice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcedureIdRequest(@JsonProperty(value = "procedure-id") String procedureId) {
}
