package es.in2.issuer.shared.domain.model.dto;

import lombok.Builder;

@Builder
public record VerifiableCredentialJWT(String token) {
}
