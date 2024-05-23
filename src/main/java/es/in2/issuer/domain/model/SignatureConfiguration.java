package es.in2.issuer.domain.model;

import es.in2.issuer.domain.model.enums.SignatureType;
import lombok.Builder;

import java.util.Map;

@Builder
public record SignatureConfiguration(SignatureType type, Map<String, String> parameters) {
}
