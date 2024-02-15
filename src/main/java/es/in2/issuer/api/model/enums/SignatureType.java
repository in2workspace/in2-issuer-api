package es.in2.issuer.api.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SignatureType {
    COSE,
    JADES
}
