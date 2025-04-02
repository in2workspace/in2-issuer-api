package es.in2.issuer.backend.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PreAuthCodeResponse(
        @JsonProperty("grant") Grant grant,
        @JsonProperty("pin") String pin
) {
}
