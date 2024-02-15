package es.in2.issuer.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProofDTO {

    @Schema(
            example = "jwt_vc_json",
            description = "Format of the proof sent"
    )
    @JsonProperty("proofType")
    private final String proofType;

    @Schema(
            example = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiw....WZwmhmn9OQp6YxX0a2L",
            description = "Contains the access token obtained with the pre-authorized code"
    )
    @JsonProperty("jwt")
    private final String jwt;
}
