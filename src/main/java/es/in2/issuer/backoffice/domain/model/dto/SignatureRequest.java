package es.in2.issuer.backoffice.domain.model.dto;

import lombok.Builder;

@Builder
public record SignatureRequest(SignatureConfiguration configuration, String data) {
}
