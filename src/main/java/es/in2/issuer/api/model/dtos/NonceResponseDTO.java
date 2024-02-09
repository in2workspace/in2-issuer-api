package es.in2.issuer.api.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NonceResponseDTO {

    @Schema(
            example = "J_8u2wAflTi2l2wQh7P_HQ",
            description = "The nonce generated"
    )
    @JsonProperty("nonce")
    private final String nonce;

    @Schema(
            example = "600",
            description = "The nonce expiration time in seconds"
    )
    @JsonProperty("nonce_expires_in")
    private final String nonceExpiresIn;
}
