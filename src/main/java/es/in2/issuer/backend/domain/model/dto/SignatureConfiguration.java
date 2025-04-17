package es.in2.issuer.backend.domain.model.dto;

import es.in2.issuer.backend.domain.model.enums.SignatureType;
import lombok.Builder;

import java.util.Map;

@Builder
public record SignatureConfiguration(SignatureType type, Map<String, String> parameters) {
}
