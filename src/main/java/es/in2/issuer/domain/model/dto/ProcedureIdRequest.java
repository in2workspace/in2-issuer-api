package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcedureIdRequest(@JsonProperty(value = "procedure-id") String procedureId) {
}
