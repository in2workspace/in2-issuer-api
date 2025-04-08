package es.in2.issuer.shared.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ResponseUriRequest(
        @JsonProperty("vc") String encodedVc
) {
}
