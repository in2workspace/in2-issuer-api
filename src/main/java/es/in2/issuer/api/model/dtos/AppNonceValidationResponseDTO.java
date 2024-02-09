package es.in2.issuer.api.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppNonceValidationResponseDTO {

    @Schema(
            example = "eyJhbGciOiJSUzI1Ni...yE1XAP-LrbAtaX-wln0wQgdXfu4dephCsMA",
            description = "The access token to store"
    )
    @JsonProperty("accessToken")
    private final String accessToken;
}
