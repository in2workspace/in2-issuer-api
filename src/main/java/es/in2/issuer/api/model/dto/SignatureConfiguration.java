package es.in2.issuer.api.model.dto;

import es.in2.issuer.api.model.enums.SignatureType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SignatureConfiguration {

    private final SignatureType type;
    private final Map<String, String> parameters;
}
