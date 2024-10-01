package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CustomJWK(
        @JsonProperty("kty") String kty,
        @JsonProperty("crv") String crv,
        @JsonProperty("x") String x,
        @JsonProperty("y") String y,
        @JsonProperty("kid") String kid
) {}