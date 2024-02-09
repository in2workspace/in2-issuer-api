package es.in2.issuer.api.model.dtos;

import es.in2.issuer.api.model.enums.SignatureType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignedData {

    private final SignatureType type;
    private final String data;

    public SignedData() {
        this(SignatureType.JADES, "");
    }
}