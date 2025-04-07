package es.in2.issuer.backoffice.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcedureIdRequest(@JsonProperty(value = "procedure-id") String procedureId) {
}
