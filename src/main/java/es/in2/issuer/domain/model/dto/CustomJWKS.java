package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record CustomJWKS(
        @JsonProperty("keys") List<CustomJWK> keys
) {}
