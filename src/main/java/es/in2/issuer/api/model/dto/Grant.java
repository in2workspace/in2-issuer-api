package es.in2.issuer.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Grant {

    @Schema(example = "1234")
    @JsonProperty("pre-authorized_code")
    private String preAuthorizedCode;

    @Schema(example = "true")
    @JsonProperty("user_pin_required")
    private boolean userPinRequired;
}
