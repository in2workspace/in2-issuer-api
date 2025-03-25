package es.in2.issuer.backend.domain.model.dto;

import lombok.Builder;

@Builder
public record SignatureRequest(SignatureConfiguration configuration, String data) {
}
