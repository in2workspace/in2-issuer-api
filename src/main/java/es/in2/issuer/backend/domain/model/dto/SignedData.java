package es.in2.issuer.backend.domain.model.dto;

import es.in2.issuer.backend.domain.model.enums.SignatureType;
import lombok.Builder;

@Builder
public record SignedData(SignatureType type, String data) {
}
