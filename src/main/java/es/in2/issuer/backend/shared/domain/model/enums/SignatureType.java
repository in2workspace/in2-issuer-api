package es.in2.issuer.backend.shared.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SignatureType {
    COSE,
    JADES
}
