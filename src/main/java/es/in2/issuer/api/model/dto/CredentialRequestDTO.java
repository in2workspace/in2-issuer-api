package es.in2.issuer.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialRequestDTO {

    @Schema(
            description = "Request body for creating a Verifiable Credential"
    )
    @JsonProperty("format")
    private final String format;

    @JsonProperty("proof")
    private final ProofDTO proof;
}
