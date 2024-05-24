package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PreAuthCodeResponse(
        @JsonProperty("grant") Grant grant,
        @JsonProperty("pin") String pin
) {
}
