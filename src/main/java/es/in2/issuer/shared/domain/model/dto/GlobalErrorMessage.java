package es.in2.issuer.shared.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GlobalErrorMessage(
        @JsonProperty("status") int status, @JsonProperty("error") String error,
        @JsonProperty("message") String message, @JsonProperty("path") String path) {
}
