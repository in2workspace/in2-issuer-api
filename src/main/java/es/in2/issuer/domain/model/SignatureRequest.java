package es.in2.issuer.domain.model;

import lombok.Builder;

@Builder
public record SignatureRequest(SignatureConfiguration configuration, String data) {
}
