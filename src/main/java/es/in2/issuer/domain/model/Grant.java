package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record Grant(@Schema(example = "1234") @JsonProperty("pre-authorized_code") String preAuthorizedCode,
                    @Schema(example = "true") @JsonProperty("user_pin_required") boolean userPinRequired) {
}
