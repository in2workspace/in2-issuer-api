package es.in2.issuer.domain.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record SignatureConfiguration(SignatureType type, Map<String, String> parameters) {
}
