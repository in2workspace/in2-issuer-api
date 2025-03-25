package es.in2.issuer.backend.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record NonceValidationResponse(
        @JsonProperty("is_nonce_valid") Boolean isNonceValid
) {

}
